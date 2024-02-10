package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KDateTimeFormat
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class KDateTimeFormatValue(value: KDateTimeFormat, symbolTable: SymbolTable) : DelegatedValue<KDateTimeFormat>(value, clazz, symbolTable = symbolTable) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KDateTimeFormat",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(CustomFunctionParameter("pattern", "String")),
            constructInstance = { interpreter, callArguments, callPosition ->
                KDateTimeFormatValue(KDateTimeFormat((callArguments[0] as StringValue).value), interpreter.symbolTable())
            }
        )
    }
}
