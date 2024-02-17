package com.sunnychung.lib.multiplatform.kotlite.model

data class FindCallableResult(
    val transformedName: String,
    val owner: String?,
    val type: CallableType,
    val isVararg: Boolean,
    val arguments: List<Any>,
    val typeParameters: List<TypeParameterNode>,
    val receiverType: TypeNode?,
    val returnType: TypeNode,
    val signature: String,
    val definition: Any,
    val scope: SymbolTable,
)
