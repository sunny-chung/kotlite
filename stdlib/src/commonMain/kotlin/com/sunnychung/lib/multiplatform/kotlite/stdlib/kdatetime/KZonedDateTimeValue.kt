package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KZoneOffset
import com.sunnychung.lib.multiplatform.kdatetime.KZonedDateTime
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun KZonedDateTimeValue(value: KZonedDateTime, symbolTable: SymbolTable) : DelegatedValue<KZonedDateTime>
    = DelegatedValue<KZonedDateTime>(value, KZonedDateTimeClass.clazz, symbolTable = symbolTable)

object KZonedDateTimeClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "KZonedDateTime",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = true,
        primaryConstructorParameters = listOf(
            CustomFunctionParameter(name = "year", type = "Int"),
            CustomFunctionParameter(name = "month", type = "Int"),
            CustomFunctionParameter(name = "day", type = "Int"),
            CustomFunctionParameter(name = "hour", type = "Int"),
            CustomFunctionParameter(name = "minute", type = "Int"),
            CustomFunctionParameter(name = "second", type = "Int"),
            CustomFunctionParameter(name = "millisecond", type = "Int", defaultValueExpression = "0"),
            CustomFunctionParameter(name = "zoneOffset", type = "KZoneOffset")
        ),
        constructInstance = { interpreter, callArguments, callPosition ->
            var i = 0
            KZonedDateTimeValue(
                KZonedDateTime(
                    year = (callArguments[i++] as IntValue).value,
                    month = (callArguments[i++] as IntValue).value,
                    day = (callArguments[i++] as IntValue).value,
                    hour = (callArguments[i++] as IntValue).value,
                    minute = (callArguments[i++] as IntValue).value,
                    second = (callArguments[i++] as IntValue).value,
                    millisecond = (callArguments[i++] as IntValue).value,
                    zoneOffset = (callArguments[i++] as DelegatedValue<KZoneOffset>).value,
                ),
                interpreter.symbolTable(),
            )
        },
        position = SourcePosition("KDateTime", 1, 1),
    )
}
