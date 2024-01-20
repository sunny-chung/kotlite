package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KZoneOffset
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition

class KZoneOffsetValue(value: KZoneOffset) : DelegatedValue<KZoneOffset>(value, clazz) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KZoneOffset",
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(
                CustomFunctionParameter(name = "hours", type = "Int"),
                CustomFunctionParameter(name = "minutes", type = "Int")
            ),
            constructInstance = { interpreter, callArguments, callPosition ->
                KZoneOffsetValue(KZoneOffset(
                    hours = (callArguments[0] as IntValue).value,
                    minutes = (callArguments[1] as IntValue).value,
                ))
            }
        )
    }
}
