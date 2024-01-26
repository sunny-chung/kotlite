package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KZonedInstant
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition

class KZonedInstantValue(value: KZonedInstant) : DelegatedValue<KZonedInstant>(value, clazz) {
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
                ))
            }
        )
    }
}