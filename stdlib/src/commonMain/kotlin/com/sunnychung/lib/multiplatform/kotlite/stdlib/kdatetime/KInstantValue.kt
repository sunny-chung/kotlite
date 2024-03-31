package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KInstant
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun KInstantValue(value: KInstant, symbolTable: SymbolTable) : DelegatedValue<KInstant>
    = DelegatedValue<KInstant>(value, KInstantClass.clazz, symbolTable = symbolTable)

object KInstantClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "KInstant",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = true,
        primaryConstructorParameters = listOf(CustomFunctionParameter("timestampMs", "Long")),
        constructInstance = { interpreter, callArguments, callPosition ->
            KInstantValue(KInstant((callArguments[0] as LongValue).value), interpreter.symbolTable())
        },
        superInterfaceTypeNames = listOf("KDateTimeFormattable"),
        superClassInvocationString = "KPointOfTime()",
        position = SourcePosition("KDateTime", 1, 1),
    )
}
