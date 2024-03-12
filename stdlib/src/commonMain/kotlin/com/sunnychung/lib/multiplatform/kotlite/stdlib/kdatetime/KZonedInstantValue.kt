package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KZonedInstant
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class KZonedInstantValue(value: KZonedInstant, symbolTable: SymbolTable) : DelegatedValue<KZonedInstant>(value, clazz, symbolTable = symbolTable) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KZonedInstant",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(
                CustomFunctionParameter(name = "timestampMs", type = "Long"),
                CustomFunctionParameter(name = "zoneOffset", type = "KZoneOffset")
            ),
            constructInstance = { interpreter, callArguments, callPosition ->
                KZonedInstantValue(KZonedInstant(
                    timestampMs = (callArguments[0] as LongValue).value,
                    zoneOffset = (callArguments[1] as KZoneOffsetValue).value,
                ), interpreter.symbolTable())
            },
            superInterfaceTypeNames = listOf("KDateTimeFormattable"),
            superInterfaces = listOf(KDateTimeFormattableInterface.interfaze),
            superClassInvocation = "KPointOfTime()",
            superClass = KPointOfTimeValue.clazz,
            position = SourcePosition("KDateTime", 1, 1),
        )
    }
}