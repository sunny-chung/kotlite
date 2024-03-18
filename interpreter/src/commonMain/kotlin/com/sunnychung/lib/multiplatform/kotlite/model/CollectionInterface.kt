package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

class CollectionInterface {

    companion object {
        val collectionClazz = ProvidedClassDefinition(
            fullQualifiedName = "Collection",
            isInterface = true,
            typeParameters = listOf(TypeParameterNode(position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1), name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
            superInterfaceTypeNames = listOf("Iterable<T>"),
//            superInterfaces = listOf(IterableInterface.clazz),
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        )

        val mutableCollectionClazz = ProvidedClassDefinition(
            fullQualifiedName = "MutableCollection",
            isInterface = true,
            typeParameters = listOf(TypeParameterNode(position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1), name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
            superInterfaceTypeNames = listOf("Collection<T>"),
//            superInterfaces = listOf(collectionClazz),
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        )
    }
}
