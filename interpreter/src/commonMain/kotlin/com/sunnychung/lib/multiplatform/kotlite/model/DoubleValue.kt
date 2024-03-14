package com.sunnychung.lib.multiplatform.kotlite.model

class DoubleValue(override val value: Double, symbolTable: SymbolTable) : NumberValue<Double>, PrimitiveValue(symbolTable) {
    override fun primitiveType(rootSymbolTable: SymbolTable) = rootSymbolTable.DoubleType
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
