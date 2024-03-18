package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KPointOfTime
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class KPointOfTimeValue(value: KPointOfTime, symbolTable: SymbolTable)
    : DelegatedValue<KPointOfTime>(value = value, clazz = clazz, symbolTable = symbolTable)
{
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KPointOfTime",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
            superInterfaceTypeNames = listOf("KDateTimeFormattable", "Comparable<KPointOfTime>"),
//            superInterfaces = listOf(KDateTimeFormattableInterface.interfaze),
            position = SourcePosition("KDateTime", 1, 1),
        )
    }
}
