package com.sunnychung.lib.multiplatform.kotlite.stdlib.regex

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue

class RegexValue(value: Regex) : DelegatedValue<Regex>(value, "Regex", clazz) {

    internal companion object {
         val clazz = ProvidedClassDefinition(
             fullQualifiedName = "Regex",
             isInstanceCreationAllowed = true,
             primaryConstructorParameters = listOf(CustomFunctionParameter("value", "String")),
             constructInstance = { interpreter, callArguments, callPosition ->
                 RegexValue(Regex((callArguments[0] as StringValue).value))
             }
         )
    }
}
