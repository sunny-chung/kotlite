package com.sunnychung.lib.multiplatform.kotlite.stdlib.range

import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun IntRangeValue(value: IntRange, symbolTable: SymbolTable)
    = DelegatedValue(value, IntRangeClass.clazz, emptyList(), symbolTable)

object IntRangeClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "IntRange",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("ClosedRange<Int>", "OpenEndRange<Int>"),
        superClassInvocationString = "IntProgression()",
        position = SourcePosition("Range", 1, 1),
    )
}
