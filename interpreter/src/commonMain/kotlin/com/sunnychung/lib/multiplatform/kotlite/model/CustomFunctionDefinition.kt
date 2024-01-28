package com.sunnychung.lib.multiplatform.kotlite.model

data class CustomFunctionDefinition(
    val receiverType: String?,
    val functionName: String,

    val returnType: String,
    val typeParameters: List<TypeParameter> = emptyList(),
    /**
     * List of arguments, which each is a pair of parameter name and data type.
     */
    val parameterTypes: List<CustomFunctionParameter>,

    val executable: (receiver: RuntimeValue?, args: List<RuntimeValue>, typeArgs: Map<String, DataType>) -> RuntimeValue,
)

class CustomFunctionParameter(val name: String, val type: String, val defaultValueExpression: String? = null, val modifiers: Set<FunctionValueParameterModifier> = emptySet())

class TypeParameter(val name: String, val typeUpperBound: String?)
