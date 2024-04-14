package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter

fun MutableMapValue(value: MutableMap<RuntimeValue, RuntimeValue>, keyType: DataType, valueType: DataType, symbolTable: SymbolTable)
    = DelegatedValue<MutableMap<RuntimeValue, RuntimeValue>>(value, MutableMapClass.clazz, listOf(keyType, valueType), symbolTable)

object MutableMapClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "MutableMap",
        typeParameters = listOf(
            TypeParameter(name = "K", typeUpperBound = null),
            TypeParameter(name = "V", typeUpperBound = null)
        ),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superClassInvocationString = "Map<K, V>()",
        position = SourcePosition("Collections", 1, 1),
    )
}
