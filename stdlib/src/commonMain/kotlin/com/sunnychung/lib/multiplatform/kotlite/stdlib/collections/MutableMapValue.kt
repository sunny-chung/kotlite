package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

fun MutableMapValue(value: Map<RuntimeValue, RuntimeValue>, keyType: DataType, valueType: DataType, symbolTable: SymbolTable)
    = DelegatedValue<Map<RuntimeValue, RuntimeValue>>(value, MutableMapClass.clazz, listOf(keyType, valueType), symbolTable)

object MutableMapClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "MutableMap",
        typeParameters = listOf(
            TypeParameterNode(position = SourcePosition("Collections", 1, 1), name = "K", typeUpperBound = null),
            TypeParameterNode(position = SourcePosition("Collections", 1, 1), name = "V", typeUpperBound = null)
        ),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superClassInvocationString = "Map<K, V>()",
        position = SourcePosition("Collections", 1, 1),
    )
}
