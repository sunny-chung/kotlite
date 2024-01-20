package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KInstant
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition

class KInstantValue(value: KInstant) : DelegatedValue<KInstant>(value, clazz) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KInstant",
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(CustomFunctionParameter("timestampMs", "Long")),
            constructInstance = { interpreter, callArguments, callPosition ->
                KInstantValue(KInstant((callArguments[0] as LongValue).value))
            }
        )
    }
}
