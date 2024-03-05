package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class MapEntryIterator<K : RuntimeValue, V : RuntimeValue>(
    private val iterator: Iterator<Map.Entry<K, V>>,
    private val keyType: DataType,
    private val valueType: DataType,
    private val symbolTable: SymbolTable,
) : Iterator<MapEntryValue> {
    override fun hasNext(): Boolean = iterator.hasNext()
    override fun next(): MapEntryValue = MapEntryValue(iterator.next(), keyType, valueType, symbolTable)
}

internal fun <K : RuntimeValue, V : RuntimeValue> Iterator<Map.Entry<K, V>>.wrap(keyType: DataType, valueType: DataType, symbolTable: SymbolTable) =
    MapEntryIterator(this, keyType, valueType, symbolTable)

