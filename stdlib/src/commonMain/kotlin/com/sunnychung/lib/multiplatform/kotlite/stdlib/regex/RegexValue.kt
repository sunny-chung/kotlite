package com.sunnychung.lib.multiplatform.kotlite.stdlib.regex

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class RegexValue(value: Regex, symbolTable: SymbolTable) : DelegatedValue<Regex>(value, "Regex", clazz, symbolTable = symbolTable) {

    internal companion object {
         val clazz = ProvidedClassDefinition(
             fullQualifiedName = "Regex",
             typeParameters = emptyList(),
             isInstanceCreationAllowed = true,
             primaryConstructorParameters = listOf(CustomFunctionParameter("value", "String")),
             constructInstance = { interpreter, callArguments, callPosition ->
                 RegexValue(Regex((callArguments[0] as StringValue).value), interpreter.symbolTable())
             },
             position = SourcePosition("Regex", 1, 1),
         )
    }
}
