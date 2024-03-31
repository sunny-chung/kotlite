package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KFixedTimeUnit
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun KFixedTimeUnitValue(value: KFixedTimeUnit, symbolTable: SymbolTable) : DelegatedValue<KFixedTimeUnit>
    = DelegatedValue<KFixedTimeUnit>(value = value, clazz = KFixedTimeUnitClass.clazz, symbolTable = symbolTable)

object KFixedTimeUnitClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "KFixedTimeUnit",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition("KDateTime", 1, 1),
    )
}
