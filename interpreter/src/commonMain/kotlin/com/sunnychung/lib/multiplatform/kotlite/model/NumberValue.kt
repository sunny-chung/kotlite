package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.extension.fullClassName

sealed interface NumberValue<T> : ComparableRuntimeValueHolder<T, Number>, RuntimeValue/*, Comparable<NumberValue<*>>*/ where T : Number {
//    val value: T

    private fun longOp(num1: NumberValue<*>, num2: NumberValue<*>, operation: (Long, Long) -> Long): LongValue? {
        if (!(num1.type() isPrimitiveTypeOf PrimitiveTypeName.Long) && !(num2.type() isPrimitiveTypeOf PrimitiveTypeName.Long)) return null
        if (num1.type() isPrimitiveTypeOf PrimitiveTypeName.Double && num2.type() isPrimitiveTypeOf PrimitiveTypeName.Double) return null

        fun NumberValue<*>.unbox(): Long {
            return if (this is IntValue) this.value.toLong() else (this as LongValue).value
        }

        return LongValue(operation(num1.unbox(), num2.unbox()), (this as PrimitiveValue).rootSymbolTable)
    }

    operator fun plus(other: NumberValue<*>): NumberValue<*> {
        if (type() isPrimitiveTypeOf PrimitiveTypeName.Int && other.type() isPrimitiveTypeOf PrimitiveTypeName.Int) {
            this as IntValue
            if (other.type() isPrimitiveTypeOf PrimitiveTypeName.Int) {
                other as IntValue
                return IntValue(value + other.value, rootSymbolTable)
            } else if (other.type() isPrimitiveTypeOf PrimitiveTypeName.Byte) {
                other as ByteValue
                return IntValue(value + other.value, rootSymbolTable)
            }
        }
        longOp(this, other) { a, b -> a + b }?.let { return it }
        val result = value.toDouble() + other.value.toDouble()
        return DoubleValue(result, (this as PrimitiveValue).rootSymbolTable)
    }
    operator fun minus(other: NumberValue<*>): NumberValue<*> {
        if (type() isPrimitiveTypeOf PrimitiveTypeName.Int) {
            this as IntValue
            if (other.type() isPrimitiveTypeOf PrimitiveTypeName.Int) {
                other as IntValue
                return IntValue(value - other.value, rootSymbolTable)
            } else if (other.type() isPrimitiveTypeOf PrimitiveTypeName.Byte) {
                other as ByteValue
                return IntValue(value - other.value, rootSymbolTable)
            }
        }
        longOp(this, other) { a, b -> a - b }?.let { return it }
        val result = value.toDouble() - other.value.toDouble()
        return DoubleValue(result, (this as PrimitiveValue).rootSymbolTable)
    }
    operator fun times(other: NumberValue<*>): NumberValue<*> {
        if (type() isPrimitiveTypeOf PrimitiveTypeName.Int && other.type() isPrimitiveTypeOf PrimitiveTypeName.Int) {
            this as IntValue
            other as IntValue
            return IntValue(value * other.value, rootSymbolTable)
        }
        longOp(this, other) { a, b -> a * b }?.let { return it }
        val result = value.toDouble() * other.value.toDouble()
        return DoubleValue(result, (this as PrimitiveValue).rootSymbolTable)
    }
    operator fun div(other: NumberValue<*>): NumberValue<*> {
        if (type() isPrimitiveTypeOf PrimitiveTypeName.Int && other.type() isPrimitiveTypeOf PrimitiveTypeName.Int) {
            this as IntValue
            other as IntValue
            return IntValue(value / other.value, rootSymbolTable)
        }
        longOp(this, other) { a, b -> a / b }?.let { return it }
        val result = value.toDouble() / other.value.toDouble()
        return DoubleValue(result, (this as PrimitiveValue).rootSymbolTable)
    }
    operator fun rem(other: NumberValue<*>): NumberValue<*> {
        if (type() isPrimitiveTypeOf PrimitiveTypeName.Int && other.type() isPrimitiveTypeOf PrimitiveTypeName.Int) {
            this as IntValue
            other as IntValue
            return IntValue(value % other.value, rootSymbolTable)
        }
        longOp(this, other) { a, b -> a % b }?.let { return it }
        val result = value.toDouble() % other.value.toDouble()
        return DoubleValue(result, (this as PrimitiveValue).rootSymbolTable)
    }

//    override fun compareTo(other: NumberValue<*>): Int {
//        if (type() is IntType && other.type() is IntType) {
//            this as IntValue
//            other as IntValue
//            return value.compareTo(other.value)
//        }
//        return (value.toDouble()).compareTo(other.value.toDouble())
//    }

    override fun compareTo(other: ComparableRuntimeValue<T, Number>): Int {
        if (other !is ComparableRuntimeValueHolder<T, Number>) {
            throw RuntimeException("Compare target is not a ComparableRuntimeValueHolder but ${other::class.fullClassName}")
        }
        return if (this !is DoubleValue) {
            val thisValue = value.toLong()
            val otherValue = other.value.toLong()
            thisValue.compareTo(otherValue)
        } else {
            val thisValue = value.toDouble()
            val otherValue = other.value.toDouble()
            thisValue.compareTo(otherValue)
        }
    }

    override fun convertToString() = (value as Number).toString()
}