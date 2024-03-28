package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.extension.fullClassName

class IntValue(override val value: Int, symbolTable: SymbolTable) : NumberValue<Int>, PrimitiveValue(symbolTable) {
    override fun primitiveType(rootSymbolTable: SymbolTable) = rootSymbolTable.IntType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString(): String {
        return "$fullClassName($value)"
    }
}
