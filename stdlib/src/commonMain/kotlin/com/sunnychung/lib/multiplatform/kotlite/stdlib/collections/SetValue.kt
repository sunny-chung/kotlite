package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter

fun SetValue(value: Set<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable)
    = DelegatedValue<Set<RuntimeValue>>(value, SetClass.clazz, listOf(typeArgument), symbolTable)

object SetClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "Set",
        typeParameters = listOf(
            TypeParameter(name = "T", typeUpperBound = null),
        ),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("Collection<T>"),
        position = SourcePosition("Collections", 1, 1),
    )
}
