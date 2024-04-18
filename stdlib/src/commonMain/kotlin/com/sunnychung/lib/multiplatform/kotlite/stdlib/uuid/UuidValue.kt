package com.sunnychung.lib.multiplatform.kotlite.stdlib.uuid

import com.benasher44.uuid.Uuid
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun UuidValue(value: Uuid, symbolTable: SymbolTable)
        = DelegatedValue<Uuid>(value, UuidClass.clazz, emptyList(), symbolTable)

object UuidClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "Uuid",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition("Uuid", 1, 1),
    )
}
