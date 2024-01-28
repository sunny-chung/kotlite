package com.sunnychung.lib.multiplatform.kotlite.model

data class CustomFunctionDefinition(
    val receiverType: String?,
    val functionName: String,

    val returnType: String,
    /**
     * List of arguments, which each is a pair of parameter name and data type.
     */
    val parameterTypes: List<CustomFunctionParameter>,

    val executable: (RuntimeValue?, List<RuntimeValue?>) -> RuntimeValue,
)

class CustomFunctionParameter(val name: String, val type: String, val defaultValueExpression: String? = null, val modifiers: Set<FunctionValueParameterModifier> = emptySet())
