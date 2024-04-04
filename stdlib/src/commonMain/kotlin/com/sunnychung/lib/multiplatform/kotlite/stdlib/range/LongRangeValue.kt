package com.sunnychung.lib.multiplatform.kotlite.stdlib.range

import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun LongRangeValue(value: LongRange, symbolTable: SymbolTable)
    = DelegatedValue(value, LongRangeClass.clazz, emptyList(), symbolTable)

object LongRangeClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "LongRange",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("ClosedRange<Long>", "OpenEndRange<Long>"),
        superClassInvocationString = "LongProgression()",
        position = SourcePosition("Range", 1, 1),
    )
}
