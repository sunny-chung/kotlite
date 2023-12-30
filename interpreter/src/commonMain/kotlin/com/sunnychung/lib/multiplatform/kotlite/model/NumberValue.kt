package com.sunnychung.lib.multiplatform.kotlite.model

sealed interface NumberValue<T : Number> : RuntimeValue, Comparable<NumberValue<*>> {
    val value: T

    operator fun plus(other: NumberValue<*>): NumberValue<*> {
        if (type() is IntType && other.type() is IntType) {
            this as IntValue
            other as IntValue
            return IntValue(value + other.value)
        }
        val result = value.toDouble() + other.value.toDouble()
        return DoubleValue(result)
    }
    operator fun minus(other: NumberValue<*>): NumberValue<*> {
        if (type() is IntType && other.type() is IntType) {
            this as IntValue
            other as IntValue
            return IntValue(value - other.value)
        }
        val result = value.toDouble() - other.value.toDouble()
        return DoubleValue(result)
    }
    operator fun times(other: NumberValue<*>): NumberValue<*> {
        if (type() is IntType && other.type() is IntType) {
            this as IntValue
            other as IntValue
            return IntValue(value * other.value)
        }
        val result = value.toDouble() * other.value.toDouble()
        return DoubleValue(result)
    }
    operator fun div(other: NumberValue<*>): NumberValue<*> {
        if (type() is IntType && other.type() is IntType) {
            this as IntValue
            other as IntValue
            return IntValue(value / other.value)
        }
        val result = value.toDouble() / other.value.toDouble()
        return DoubleValue(result)
    }
    operator fun rem(other: NumberValue<*>): NumberValue<*> {
        if (type() is IntType && other.type() is IntType) {
            this as IntValue
            other as IntValue
            return IntValue(value % other.value)
        }
        val result = value.toDouble() % other.value.toDouble()
        return DoubleValue(result)
    }

    override fun compareTo(other: NumberValue<*>): Int {
        if (type() is IntType && other.type() is IntType) {
            this as IntValue
            other as IntValue
            return value.compareTo(other.value)
        }
        return (value.toDouble()).compareTo(other.value.toDouble())
    }
}