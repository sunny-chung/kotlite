package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.CollectionInterface
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

fun MutableSetValue(value: MutableSet<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable)
    = DelegatedValue<MutableSet<RuntimeValue>>(value, MutableSetClass.clazz, listOf(typeArgument), symbolTable)

object MutableSetClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "MutableSet",
        typeParameters = listOf(TypeParameterNode(position = SourcePosition("Collections", 1, 1), name = "T", typeUpperBound = null)),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("MutableCollection<T>"),
        superClassInvocationString = "Set<T>()",
        position = SourcePosition("Collections", 1, 1),
    )
}
