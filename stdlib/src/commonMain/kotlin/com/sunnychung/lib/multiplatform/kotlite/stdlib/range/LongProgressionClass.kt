package com.sunnychung.lib.multiplatform.kotlite.stdlib.range

import com.sunnychung.lib.multiplatform.kotlite.model.ClassModifier
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun LongProgressionValue(value: LongProgression, symbolTable: SymbolTable)
    = DelegatedValue(value, LongProgressionClass.clazz, emptyList(), symbolTable)

object LongProgressionClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "LongProgression",
        typeParameters = emptyList(),
        modifiers = setOf(ClassModifier.open),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("PrimitiveIterable<Long>"),
        position = SourcePosition("Range", 1, 1),
    )
}
