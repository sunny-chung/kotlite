package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KInstant
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class KInstantValue(value: KInstant, symbolTable: SymbolTable) : DelegatedValue<KInstant>(value, clazz, symbolTable = symbolTable) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KInstant",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(CustomFunctionParameter("timestampMs", "Long")),
            constructInstance = { interpreter, callArguments, callPosition ->
                KInstantValue(KInstant((callArguments[0] as LongValue).value), interpreter.symbolTable())
            },
            superInterfaceTypeNames = listOf("KDateTimeFormattable"),
//            superInterfaces = listOf(KDateTimeFormattableInterface.interfaze),
            superClassInvocationString = "KPointOfTime()",
//            superClass = KPointOfTimeValue.clazz,
            position = SourcePosition("KDateTime", 1, 1),
        )
    }
}
