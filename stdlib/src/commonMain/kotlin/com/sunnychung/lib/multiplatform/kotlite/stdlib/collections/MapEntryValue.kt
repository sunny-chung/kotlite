package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

fun MapEntryValue(value: Map.Entry<RuntimeValue, RuntimeValue>, keyType: DataType, valueType: DataType, symbolTable: SymbolTable)
    = DelegatedValue<Map.Entry<RuntimeValue, RuntimeValue>>(value, MapEntryClass.clazz, listOf(keyType, valueType), symbolTable)

object MapEntryClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "MapEntry",
        typeParameters = listOf(
            TypeParameterNode(position = SourcePosition("Collections", 1, 1), name = "K", typeUpperBound = null),
            TypeParameterNode(position = SourcePosition("Collections", 1, 1), name = "V", typeUpperBound = null)
        ),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition("Collections", 1, 1),
    )
}
