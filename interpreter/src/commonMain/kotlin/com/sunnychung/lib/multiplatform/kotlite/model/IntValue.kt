package com.sunnychung.lib.multiplatform.kotlite.model

data class IntValue(val value: Int) : RuntimeValue, Comparable<IntValue> {
    operator fun plus(other: IntValue): IntValue {
        return IntValue(value!! + other.value!!)
    }
    operator fun minus(other: IntValue): IntValue {
        return IntValue(value!! - other.value!!)
    }
    operator fun times(other: IntValue): IntValue {
        return IntValue(value!! * other.value!!)
    }
    operator fun div(other: IntValue): IntValue {
        return IntValue(value!! / other.value!!)
    }
    operator fun rem(other: IntValue): IntValue {
        return IntValue(value!! % other.value!!)
    }

    override fun compareTo(other: IntValue): Int {
        return value.compareTo(other.value)
    }
}
