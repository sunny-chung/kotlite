package com.sunnychung.lib.multiplatform.kotlite.model

class IterableInterface {

    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "Iterable",
            typeParameters = listOf(TypeParameterNode(name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        )
    }
}
