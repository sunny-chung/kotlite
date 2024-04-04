package com.sunnychung.lib.multiplatform.kotlite.stdlib.range

import com.sunnychung.lib.multiplatform.kotlite.model.ClassModifier
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun IntProgressionValue(value: IntProgression, symbolTable: SymbolTable)
    = DelegatedValue(value, IntProgressionClass.clazz, emptyList(), symbolTable)

object IntProgressionClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "IntProgression",
        typeParameters = emptyList(),
        modifiers = setOf(ClassModifier.open),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("PrimitiveIterable<Int>"),
        position = SourcePosition("Range", 1, 1),
    )
}
