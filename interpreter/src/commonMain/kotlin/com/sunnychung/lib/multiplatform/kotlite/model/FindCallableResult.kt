package com.sunnychung.lib.multiplatform.kotlite.model

data class FindCallableResult(
    val transformedName: String,
    val owner: String?,
    val type: CallableType,
    val arguments: List<Any>,
    val typeParameters: List<TypeParameterNode>,
    val returnType: TypeNode,
    val definition: Any,
    val scope: SymbolTable,
)
