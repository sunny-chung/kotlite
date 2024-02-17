package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

class MapValue(value: Map<RuntimeValue, RuntimeValue>, keyType: DataType, valueType: DataType, symbolTable: SymbolTable)
    : DelegatedValue<Map<RuntimeValue, RuntimeValue>>(value, clazz, listOf(keyType, valueType), symbolTable) {

    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "Map",
            typeParameters = listOf(
                TypeParameterNode(name = "K", typeUpperBound = null),
                TypeParameterNode(name = "V", typeUpperBound = null)
            ),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        )
    }
}
