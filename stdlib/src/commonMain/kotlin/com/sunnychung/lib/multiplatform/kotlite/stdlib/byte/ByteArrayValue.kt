package com.sunnychung.lib.multiplatform.kotlite.stdlib.byte

import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun ByteArrayValue(value: ByteArray, symbolTable: SymbolTable)
    = DelegatedValue<ByteArray>(value, ByteArrayClass.clazz, emptyList(), symbolTable)

object ByteArrayClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "ByteArray",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition("Byte", 1, 1),
    )
}
