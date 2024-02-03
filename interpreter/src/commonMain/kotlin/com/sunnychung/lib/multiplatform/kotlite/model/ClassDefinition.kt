package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter

open class ClassDefinition(
    currentScope: SymbolTable?,

    val name: String,
    val fullQualifiedName: String = name, // TODO

    /**
     * If it is an object class, no new instance can be created
     */
    val isInstanceCreationAllowed: Boolean,

    val typeParameters: List<TypeParameterNode>,

    /**
     * Only contains ClassInstanceInitializerNode and PropertyDeclarationNode
     */
    val orderedInitializersAndPropertyDeclarations: List<ASTNode>,

    rawMemberProperties: List<PropertyDeclarationNode>,
    val memberFunctions: Map<String, FunctionDeclarationNode>,

    val primaryConstructor: ClassPrimaryConstructorNode?,
) {
    // key = original name
    // does not include properties with custom accessors
    val memberProperties: Map<String, PropertyType> = mutableMapOf()
    // key = original name
    val memberPropertyTypes: Map<String, PropertyType> = mutableMapOf()
    // key = original name
    val memberPropertyCustomAccessors: Map<String, PropertyAccessorsNode> = mutableMapOf()
    val memberTransformedNameToPropertyName: Map<String, String> = mutableMapOf()
    val memberPropertyNameToTransformedName: Map<String, String> = mutableMapOf()

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

    fun findMemberFunctionsByDeclaredName(declaredName: String) =
        memberFunctions.filter { it.value.name == declaredName }

    open fun construct(interpreter: Interpreter, callArguments: Array<RuntimeValue>, typeArguments: Array<DataType>, callPosition: SourcePosition): ClassInstance {
        return interpreter.constructClassInstance(callArguments, callPosition, typeArguments, this@ClassDefinition)
    }
}
