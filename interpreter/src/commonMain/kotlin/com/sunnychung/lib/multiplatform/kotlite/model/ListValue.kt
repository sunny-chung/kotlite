package com.sunnychung.lib.multiplatform.kotlite.model

class ListValue(value: List<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable) : DelegatedValue<List<RuntimeValue>>(value, clazz, listOf(typeArgument), symbolTable) {

    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "List",
            typeParameters = listOf(TypeParameterNode(name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        )
    }
}