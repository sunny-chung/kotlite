package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

class ListValue(value: List<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable) : DelegatedValue<List<RuntimeValue>>(value, clazz, listOf(typeArgument), symbolTable) {

    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "List",
            typeParameters = listOf(TypeParameterNode(SourcePosition.BUILTIN, name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
            superInterfaceTypeNames = listOf("Collection<T>"),
//            superInterfaces = listOf(CollectionInterface.collectionClazz),
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        )
    }
}
