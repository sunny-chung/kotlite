package com.sunnychung.lib.multiplatform.kotlite.model

sealed interface RuntimeValue {
    fun type(): DataType
}
//data class RuntimeValue(val type: DataType, val value: Any?)

data object UnitValue : RuntimeValue {
    override fun type() = UnitType()
}
data object NullValue : RuntimeValue {
    override fun type() = NullType
}

data class BooleanValue(val value: Boolean) : RuntimeValue {
    override fun type() = BooleanType()
}
