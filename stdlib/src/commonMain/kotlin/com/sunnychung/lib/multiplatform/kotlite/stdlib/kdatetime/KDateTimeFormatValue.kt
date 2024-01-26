package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KDateTimeFormat
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue

class KDateTimeFormatValue(value: KDateTimeFormat) : DelegatedValue<KDateTimeFormat>(value, clazz) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KDateTimeFormat",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(CustomFunctionParameter("pattern", "String")),
            constructInstance = { interpreter, callArguments, callPosition ->
                KDateTimeFormatValue(KDateTimeFormat((callArguments[0] as StringValue).value))
            }
        )
    }
}
