package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KZoneOffset
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class KZoneOffsetValue(value: KZoneOffset, symbolTable: SymbolTable) : DelegatedValue<KZoneOffset>(value, clazz, symbolTable = symbolTable) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KZoneOffset",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(
                CustomFunctionParameter(name = "hours", type = "Int"),
                CustomFunctionParameter(name = "minutes", type = "Int")
            ),
            constructInstance = { interpreter, callArguments, callPosition ->
                KZoneOffsetValue(KZoneOffset(
                    hours = (callArguments[0] as IntValue).value,
                    minutes = (callArguments[1] as IntValue).value,
                ), interpreter.symbolTable())
            },
            position = SourcePosition("KDateTime", 1, 1),
        )
    }
}
