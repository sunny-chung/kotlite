package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class MapEntryIterator<K : RuntimeValue, V : RuntimeValue>(
    private val iterator: Iterator<Map.Entry<K, V>>,
    private val keyType: DataType,
    private val valueType: DataType,
    private val symbolTable: SymbolTable,
) : Iterator<DelegatedValue<Map.Entry<RuntimeValue, RuntimeValue>>> {
    override fun hasNext(): Boolean = iterator.hasNext()
    override fun next(): DelegatedValue<Map.Entry<RuntimeValue, RuntimeValue>> = MapEntryValue(iterator.next(), keyType, valueType, symbolTable)
}

internal fun <K : RuntimeValue, V : RuntimeValue> Iterator<Map.Entry<K, V>>.wrap(keyType: DataType, valueType: DataType, symbolTable: SymbolTable) =
    MapEntryIterator(this, keyType, valueType, symbolTable)

