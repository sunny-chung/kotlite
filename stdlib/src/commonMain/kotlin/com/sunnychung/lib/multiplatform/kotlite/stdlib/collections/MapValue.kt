package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter

fun MapValue(value: Map<RuntimeValue, RuntimeValue>, keyType: DataType, valueType: DataType, symbolTable: SymbolTable)
    = DelegatedValue<Map<RuntimeValue, RuntimeValue>>(value, MapClass.clazz, listOf(keyType, valueType), symbolTable)

object MapClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "Map",
        typeParameters = listOf(
            TypeParameter(name = "K", typeUpperBound = null),
            TypeParameter(name = "V", typeUpperBound = null)
        ),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition("Collections", 1, 1),
    )
}
