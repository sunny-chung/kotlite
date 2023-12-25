package com.sunnychung.lib.multiplatform.kotlite.model

sealed interface RuntimeValue
//data class RuntimeValue(val type: DataType, val value: Any?)

enum class DataType {
    Integer
}

data object UnitValue : RuntimeValue
data object NullValue : RuntimeValue

data class BooleanValue(val value: Boolean) : RuntimeValue
