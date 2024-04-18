package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter

/**
 * Its main use cases are to allow mutations to member properties of nested class instances and in lambdas.
 */
interface RuntimeValueAccessor {
    val type: DataType

    fun assign(interpreter: Interpreter? = null, value: RuntimeValue)
    fun read(interpreter: Interpreter? = null): RuntimeValue
}

class RuntimeValueHolder(override val type: DataType, val isMutable: Boolean, value: RuntimeValue? = null) : RuntimeValueAccessor {
    internal var value: RuntimeValue? = null

    init {
        if (value != null) {
            assign(value = value)
        }
    }

    override fun assign(interpreter: Interpreter?, value: RuntimeValue) {
        if (!isMutable && this.value != null) {
            throw RuntimeException("val cannot be reassigned")
        }
        if (!type.isCastableFrom(value.type()) && type != value.type()) {
            throw RuntimeException("Type ${value.type().descriptiveName} cannot be casted to ${type.descriptiveName}")
        }
        this.value = value
    }

    override fun read(interpreter: Interpreter?) = value!!

    override fun toString(): String = value.toString()
}

/**
 * For class members with custom accessors
 */
class RuntimeValueDelegate(override val type: DataType, val reader: ((Interpreter?) -> RuntimeValue)?, val writer: ((Interpreter?, RuntimeValue) -> Unit)?) : RuntimeValueAccessor {
    override fun assign(interpreter: Interpreter?, value: RuntimeValue) {
        if (writer == null) throw RuntimeException("Setter is not defined")
        writer!!(interpreter, value)
    }

    override fun read(interpreter: Interpreter?): RuntimeValue {
        if (reader == null) throw RuntimeException("Getter is not defined")
        return reader!!(interpreter)
    }
}
