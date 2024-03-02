package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter

data class CustomFunctionDefinition(
    val position: SourcePosition,

    val receiverType: String?,
    val functionName: String,

    val returnType: String,
    val typeParameters: List<TypeParameter> = emptyList(),
    /**
     * List of arguments, which each is a pair of parameter name and data type.
     */
    val parameterTypes: List<CustomFunctionParameter>,
    val modifiers: Set<FunctionModifier> = emptySet(),

    val executable: (interpreter: Interpreter, receiver: RuntimeValue?, args: List<RuntimeValue>, typeArgs: Map<String, DataType>) -> RuntimeValue,
)

class CustomFunctionParameter(val name: String, val type: String, val defaultValueExpression: String? = null, val modifiers: Set<String> = emptySet())

class TypeParameter(val name: String, val typeUpperBound: String?)
fun TypeParameter.toTypeParameterNode() = TypeParameterNode(SourcePosition.NONE, this.name, this.typeUpperBound?.toTypeNode("TODO"))
fun List<TypeParameter>.toTypeParameterNodes() = this.map { it.toTypeParameterNode() }
