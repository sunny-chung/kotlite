package com.sunnychung.lib.multiplatform.kotlite.model

class CustomFunctionDefinition(
    val receiverType: String?,
    val functionName: String,

    val returnType: String,
    /**
     * List of arguments, which each is a pair of parameter name and data type.
     */
    val parameterTypes: List<Pair<String, String>>,

    val executable: (RuntimeValue?, List<RuntimeValue?>) -> RuntimeValue,
)
