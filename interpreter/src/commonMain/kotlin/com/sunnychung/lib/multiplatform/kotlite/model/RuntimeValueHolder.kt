package com.sunnychung.lib.multiplatform.kotlite.model

/**
 * Its main use cases are to allow mutations to member properties of nested class instances and in lambdas.
 */
interface RuntimeValueAccessor {
    val type: DataType

    fun assign(value: RuntimeValue)
    fun read(): RuntimeValue
}

class RuntimeValueHolder(override val type: DataType, val isMutable: Boolean, value: RuntimeValue? = null) : RuntimeValueAccessor {
    internal var value: RuntimeValue? = null

    init {
        if (value != null) {
            assign(value)
        }
    }

    override fun assign(value: RuntimeValue) {
        if (!isMutable && this.value != null) {
            throw RuntimeException("val cannot be reassigned")
        }
        if (!type.isAssignableFrom(value.type())) {
            throw RuntimeException("Type ${value.type().nameWithNullable} cannot be casted to ${type.nameWithNullable}")
        }
        this.value = value
    }

    override fun read() = value!!

    override fun toString(): String = value.toString()
}

/**
 * For class members with custom accessors
 */
class RuntimeValueDelegate(override val type: DataType, val reader: (() -> RuntimeValue)?, val writer: ((RuntimeValue) -> Unit)?) : RuntimeValueAccessor {
    override fun assign(value: RuntimeValue) {
        writer!!(value)
    }

    override fun read() = reader!!()
}
