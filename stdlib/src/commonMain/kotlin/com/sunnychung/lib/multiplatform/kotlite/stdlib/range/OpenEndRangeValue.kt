package com.sunnychung.lib.multiplatform.kotlite.stdlib.range

import com.sunnychung.lib.multiplatform.kotlite.model.ComparableRuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter

fun OpenEndRangeValue(value: OpenEndRange<ComparableRuntimeValue<Comparable<Any>, Any>>, valueType: DataType, symbolTable: SymbolTable)
    = DelegatedValue(value, OpenEndRangeClass.clazz, listOf(valueType), symbolTable)

object OpenEndRangeClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "OpenEndRange",
        typeParameters = listOf(
            TypeParameter(name = "T", typeUpperBound = "Comparable<T>"),
        ),
        isInstanceCreationAllowed = false,
        isInterface = true,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition("Range", 1, 1),
    )
}
