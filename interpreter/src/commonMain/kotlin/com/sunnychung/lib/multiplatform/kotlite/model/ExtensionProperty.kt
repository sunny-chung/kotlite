package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.annotation.ModifyByAnalyzer

class ExtensionProperty(
    val declaredName: String,
    val typeParameters: List<TypeParameter> = emptyList(),
    val receiver: String,
    val type: String,
    val getter: ((receiver: RuntimeValue) -> RuntimeValue)? = null,
    val setter: ((receiver: RuntimeValue, value: RuntimeValue) -> Unit)? = null,
) {
    init {
        if (getter == null && setter == null) {
            throw IllegalArgumentException("Missing getter or setter")
        }
    }

    internal @ModifyByAnalyzer var transformedName: String? = null
    internal @ModifyByAnalyzer var typeNode: TypeNode? = null
    internal @ModifyByAnalyzer var receiverType: TypeNode? = null
}
