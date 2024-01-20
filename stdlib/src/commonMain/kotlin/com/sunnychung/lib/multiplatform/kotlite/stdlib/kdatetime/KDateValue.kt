package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KDate
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition

class KDateValue(value: KDate) : DelegatedValue<KDate>(value, clazz) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KDate",
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(
                CustomFunctionParameter(name = "year", type = "Int"),
                CustomFunctionParameter(name = "month", type = "Int"),
                CustomFunctionParameter(name = "day", type = "Int"),
            ),
            constructInstance = { interpreter, callArguments, callPosition ->
                var i = 0
                KDateValue(
                    KDate(
                        year = (callArguments[i++] as IntValue).value,
                        month = (callArguments[i++] as IntValue).value,
                        day = (callArguments[i++] as IntValue).value,
                    )
                )
            }
        )
    }
}