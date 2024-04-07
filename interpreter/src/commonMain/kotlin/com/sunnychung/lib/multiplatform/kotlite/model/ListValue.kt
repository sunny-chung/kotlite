package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

fun ListValue(value: List<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable)
    = DelegatedValue<List<RuntimeValue>>(value, ListClass.clazz, listOf(typeArgument), symbolTable)

object ListClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "List",
        typeParameters = listOf(TypeParameter(name = "T", typeUpperBound = null)),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("Collection<T>"),
        position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
    )
}
