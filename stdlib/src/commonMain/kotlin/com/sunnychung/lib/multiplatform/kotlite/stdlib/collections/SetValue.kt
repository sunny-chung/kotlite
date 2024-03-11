package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.CollectionInterface
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

class SetValue(value: Set<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable)
    : DelegatedValue<Set<RuntimeValue>>(value, clazz, listOf(typeArgument), symbolTable) {

    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "Set",
            typeParameters = listOf(
                TypeParameterNode(position = SourcePosition("Collections", 1, 1), name = "T", typeUpperBound = null),
            ),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
            superInterfaceTypeNames = listOf("Collection<T>"),
            superInterfaces = listOf(CollectionInterface.collectionClazz),
            position = SourcePosition("Collections", 1, 1),
        )
    }
}
