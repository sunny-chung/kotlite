package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.extension.merge
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

    // key = original name
    // does not include properties with custom accessors
    private val memberProperties: Map<String, PropertyType> = mutableMapOf()
    // key = original name
    private val memberPropertyTypes: Map<String, PropertyType> = mutableMapOf()
    // key = original name
    private val memberPropertyCustomAccessors: Map<String, PropertyAccessorsNode> = mutableMapOf()
    private val memberTransformedNameToPropertyName: Map<String, String> = mutableMapOf()
    private val memberPropertyNameToTransformedName: Map<String, String> = mutableMapOf()

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
            PropertyType(ObjectType(this, it.type.arguments?.map { currentScope!!.typeNodeToDataType(it)!! } ?: emptyList(), it.type.isNullable), it.isMutable)
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

    fun getAllMemberProperties(): Map<String, PropertyType> {
        return memberProperties mergeIfNotExists (superClass?.getAllMemberProperties() ?: emptyMap())
    }

    fun getDeclaredPropertiesInThisClass() = memberProperties
    fun getDeclaredPropertyAccessorsInThisClass() = memberPropertyCustomAccessors

    fun findMemberProperty(declaredName: String, inThisClassOnly: Boolean = false) : PropertyType? =
        memberPropertyTypes[declaredName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberProperty(declaredName, inThisClassOnly) }

    fun findMemberPropertyWithoutAccessor(declaredName: String, inThisClassOnly: Boolean = false): PropertyType? =
        memberProperties[declaredName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyWithoutAccessor(declaredName, inThisClassOnly) }

    fun findMemberPropertyCustomAccessor(declaredName: String, inThisClassOnly: Boolean = false): PropertyAccessorsNode? =
        memberPropertyCustomAccessors[declaredName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyCustomAccessor(declaredName, inThisClassOnly) }

    fun findMemberPropertyTransformedName(declaredName: String, inThisClassOnly: Boolean = false): String? =
        memberPropertyNameToTransformedName[declaredName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyTransformedName(declaredName, inThisClassOnly) }

    fun findMemberPropertyDeclaredName(transformedName: String, inThisClassOnly: Boolean = false): String? =
        memberTransformedNameToPropertyName[transformedName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyDeclaredName(transformedName, inThisClassOnly) }

    fun getAllMemberFunctions(): Map<String, FunctionDeclarationNode> {
        return memberFunctions mergeIfNotExists (superClass?.getAllMemberFunctions() ?: emptyMap())
    }

    fun findMemberFunctionsByDeclaredName(declaredName: String, inThisClassOnly: Boolean = false): Map<String, FunctionDeclarationNode> =
        memberFunctions.filter { it.value.name == declaredName } mergeIfNotExists
            (Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberFunctionsByDeclaredName(declaredName, inThisClassOnly) } ?: emptyMap() )

    fun findMemberFunctionByTransformedName(transformedName: String, inThisClassOnly: Boolean = false): FunctionDeclarationNode? =
        memberFunctions[transformedName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberFunctionByTransformedName(transformedName, inThisClassOnly) }

    open fun construct(interpreter: Interpreter, callArguments: Array<RuntimeValue>, typeArguments: Array<DataType>, callPosition: SourcePosition): ClassInstance {
        return interpreter.constructClassInstance(callArguments, callPosition, typeArguments, this@ClassDefinition)
    }
}
