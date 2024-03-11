package com.sunnychung.lib.multiplatform.kotlite.model

data class FindCallableResult(
    val transformedName: String,
    val originalName: String,
    val owner: String?,
    val type: CallableType,
    val isVararg: Boolean,
    val arguments: List<Any>, // either DataType or FunctionValueParameterNode
    val typeParameters: List<TypeParameterNode>,
    val receiverType: TypeNode?,
    val returnType: TypeNode,
    val signature: String,
    val definition: Any,
    val scope: SymbolTable,
) {
    fun toDisplayableSignature() = buildString {
        if (receiverType != null) {
            append("${receiverType.descriptiveName()}.")
        }
        append(originalName)
        append("(")
        append(arguments.joinToString(", ") { when (it) {
            is DataType -> it.descriptiveName
            is FunctionValueParameterNode -> it.type.descriptiveName()
            else -> throw UnsupportedOperationException()
        } })
        append(")")
    }
}
