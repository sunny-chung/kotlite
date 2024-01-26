package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KZonedDateTime
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition

class KZonedDateTimeValue(value: KZonedDateTime) : DelegatedValue<KZonedDateTime>(value, clazz) {
    companion object {
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
                        zoneOffset = (callArguments[i++] as KZoneOffsetValue).value,
                    )
                )
            }
        )
    }
}