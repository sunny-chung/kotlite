package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.extension.mergeIfNotExists

open class ClassDefinition(
    /**
     * For storing parsed declarations
     */
    internal val currentScope: SymbolTable?,

    val name: String,
    val fullQualifiedName: String = name, // TODO

    /**
     * If it is an object class, no new instance can be created
     */
    val isInstanceCreationAllowed: Boolean,
    val modifiers: Set<ClassModifier>,

    val typeParameters: List<TypeParameterNode>,

    /**
     * Only contains ClassInstanceInitializerNode and PropertyDeclarationNode
     */
    val orderedInitializersAndPropertyDeclarations: List<ASTNode>,

    val declarations: List<ASTNode>,

    rawMemberProperties: List<PropertyDeclarationNode>,
    private val memberFunctions: Map<String, FunctionDeclarationNode>,

    val primaryConstructor: ClassPrimaryConstructorNode?,
    val superClassInvocation: FunctionCallNode? = null,
    val superClass: ClassDefinition? = null
) {

    init {
        if (superClass != null && superClassInvocation == null) {
            throw RuntimeException("superClassInvocation must be provided if there is a supper class")
        }
    }

    // key = original name
    // does not include properties with custom accessors
    private val memberProperties: Map<String, PropertyType> = mutableMapOf()
    // key = original name
    private val memberPropertyTypes: Map<String, PropertyType> = mutableMapOf()
    // key = original name
    private val memberPropertyCustomAccessors: Map<String, PropertyAccessorsNode> = mutableMapOf()
    private val memberTransformedNameToPropertyName: Map<String, String> = mutableMapOf()
    private val memberPropertyNameToTransformedName: Map<String, String> = mutableMapOf()

    private fun findIndex(): Int {
        if (superClass == null) return 0
        return superClass.findIndex() + 1
    }

    val index = findIndex()

    init {
        rawMemberProperties.forEach { addProperty(currentScope, it) }
    }

    /**
     * Only for SemanticAnalyzer use during parsing class declarations.
     */
    internal fun addProperty(currentScope: SymbolTable?, it: PropertyDeclarationNode) {
        val type = (currentScope!!.typeNodeToPropertyType(
            it.type,
            it.isMutable
        ) ?: if (it.type.name == name) {
            val superType = currentScope!!.resolveObjectType(this, this.typeParameters.map { TypeNode(it.position, it.name, null, false) }, it.type.isNullable, upToIndex = index - 1)
            PropertyType(ObjectType(this, it.type.arguments?.map { currentScope!!.typeNodeToDataType(it)!! } ?: emptyList(), it.type.isNullable, superType), it.isMutable)
//            PropertyType(ObjectType(this, it.type.arguments?.map { currentScope!!.typeNodeToDataType(it)!! } ?: emptyList(), it.type.isNullable), it.isMutable)
        } else throw RuntimeException("Unknown type ${it.type.name}"))
        (memberPropertyTypes as MutableMap)[it.name] = type
        if (it.accessors == null) {
            (memberProperties as MutableMap)[it.name] = type
        } else {
            (memberPropertyCustomAccessors as MutableMap)[it.name] = it.accessors
        }

        if (it.transformedRefName != null) {
            (memberTransformedNameToPropertyName as MutableMap)[it.transformedRefName!!] = it.name
            (memberPropertyNameToTransformedName as MutableMap)[it.name] = it.transformedRefName!!
        }
    }


    /**
     * Key: Original declared name
     */
    fun getAllMemberPropertiesExcludingCustomAccessors(): Map<String, PropertyType> {
        return memberProperties mergeIfNotExists (superClass?.getAllMemberPropertiesExcludingCustomAccessors() ?: emptyMap())
    }

    /**
     * Key: Original declared name
     */
    fun getAllMemberProperties(): Map<String, PropertyType> {
        return memberPropertyTypes mergeIfNotExists (superClass?.getAllMemberProperties() ?: emptyMap())
    }

    fun getDeclaredPropertiesInThisClass() = memberProperties
    fun getDeclaredPropertyAccessorsInThisClass() = memberPropertyCustomAccessors

    fun findMemberPropertyWithIndex(declaredName: String, inThisClassOnly: Boolean = false) : Pair<PropertyType, Int>? =
        memberPropertyTypes[declaredName]?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyWithIndex(declaredName, inThisClassOnly) }

    fun findMemberProperty(declaredName: String, inThisClassOnly: Boolean = false) =
        findMemberPropertyWithIndex(declaredName, inThisClassOnly)?.first

    fun findMemberPropertyWithoutAccessorWithIndex(declaredName: String, inThisClassOnly: Boolean = false): Pair<PropertyType, Int>? =
        memberProperties[declaredName]?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyWithoutAccessorWithIndex(declaredName, inThisClassOnly) }

    fun findMemberPropertyWithoutAccessor(declaredName: String, inThisClassOnly: Boolean = false) =
        findMemberPropertyWithoutAccessorWithIndex(declaredName, inThisClassOnly)?.first

    fun findMemberPropertyCustomAccessorWithIndex(declaredName: String, inThisClassOnly: Boolean = false): Pair<PropertyAccessorsNode, Int>? =
        memberPropertyCustomAccessors[declaredName]?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyCustomAccessorWithIndex(declaredName, inThisClassOnly) }

    fun findMemberPropertyCustomAccessor(declaredName: String, inThisClassOnly: Boolean = false): PropertyAccessorsNode? =
        findMemberPropertyCustomAccessorWithIndex(declaredName, inThisClassOnly)?.first

    fun findMemberPropertyTransformedName(declaredName: String, inThisClassOnly: Boolean = false): String? =
        memberPropertyNameToTransformedName[declaredName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyTransformedName(declaredName, inThisClassOnly) }

    fun findMemberPropertyDeclaredName(transformedName: String, inThisClassOnly: Boolean = false): String? =
        memberTransformedNameToPropertyName[transformedName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyDeclaredName(transformedName, inThisClassOnly) }

    /**
     * Key: Function signature
     */
    fun getAllMemberFunctions(): Map<String, FunctionDeclarationNode> {
        return memberFunctions mergeIfNotExists (superClass?.getAllMemberFunctions() ?: emptyMap())
    }

    fun findMemberFunctionsWithIndexByDeclaredName(declaredName: String, inThisClassOnly: Boolean = false): Map<String, Pair<FunctionDeclarationNode, Int>> =
        memberFunctions.filter { it.value.name == declaredName }.mapValues { it.value to index } mergeIfNotExists
            (Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberFunctionsWithIndexByDeclaredName(declaredName, inThisClassOnly) } ?: emptyMap() )

    fun findMemberFunctionsByDeclaredName(declaredName: String, inThisClassOnly: Boolean = false): Map<String, FunctionDeclarationNode> =
        findMemberFunctionsWithIndexByDeclaredName(declaredName, inThisClassOnly).mapValues { it.value.first }

    fun findMemberFunctionWithIndexByTransformedName(transformedName: String, inThisClassOnly: Boolean = false): Pair<FunctionDeclarationNode, Int>? =
        memberFunctions[transformedName]?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberFunctionWithIndexByTransformedName(transformedName, inThisClassOnly) }

    /**
     * For semantic analyzer use only. In SA, `memberFunctions` is not indexed by transformedRefName.
     */
    fun findMemberFunctionWithIndexByTransformedNameLinearSearch(transformedName: String, inThisClassOnly: Boolean = false): Pair<FunctionDeclarationNode, Int>? =
        memberFunctions.filter { it.value.transformedRefName == transformedName }.values.firstOrNull()?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberFunctionWithIndexByTransformedNameLinearSearch(transformedName, inThisClassOnly) }

    fun findMemberFunctionByTransformedName(transformedName: String, inThisClassOnly: Boolean = false): FunctionDeclarationNode? =
        findMemberFunctionWithIndexByTransformedName(transformedName, inThisClassOnly)?.first

    fun findDeclarations(filter: (clazz: ClassDefinition, declaration: ASTNode) -> Boolean): List<ASTNode> {
        return declarations.filter { filter(this, it) } +
            (superClass?.findDeclarations(filter) ?: emptyList())
    }

    open fun construct(interpreter: Interpreter, callArguments: Array<RuntimeValue>, typeArguments: Array<DataType>, callPosition: SourcePosition): ClassInstance {
        return interpreter.constructClassInstance(callArguments, callPosition, typeArguments, this@ClassDefinition)
    }
}
