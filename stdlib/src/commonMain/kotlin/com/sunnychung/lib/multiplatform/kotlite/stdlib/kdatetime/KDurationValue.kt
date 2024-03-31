package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KDuration
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun KDurationValue(value: KDuration, symbolTable: SymbolTable)
    = DelegatedValue<KDuration>(value, KDurationClass.clazz, symbolTable = symbolTable)

object KDurationClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "KDuration",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { interpreter, callArguments, callPosition ->
            throw UnsupportedOperationException()
        },
        superInterfaceTypeNames = listOf("KDateTimeFormattable", "Comparable<KDuration>"),
        position = SourcePosition("KDateTime", 1, 1),
    )
}
