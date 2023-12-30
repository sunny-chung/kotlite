package com.sunnychung.lib.multiplatform.kotlite.model

class SemanticDummyRuntimeValue(private val dataType: DataType) : RuntimeValue {
    override fun type(): DataType = dataType
}
