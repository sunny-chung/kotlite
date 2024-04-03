package com.sunnychung.lib.multiplatform.kotlite.stdlib.byte

import com.sunnychung.lib.multiplatform.kotlite.model.ByteValue
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class ByteValueIterator(
    private val iterator: Iterator<Byte>,
    private val symbolTable: SymbolTable,
) : Iterator<RuntimeValue> {
    override fun hasNext(): Boolean = iterator.hasNext()
    override fun next(): ByteValue = ByteValue(iterator.next(), symbolTable)
}

internal fun Iterator<Byte>.wrap(symbolTable: SymbolTable) =
    ByteValueIterator(this, symbolTable)
