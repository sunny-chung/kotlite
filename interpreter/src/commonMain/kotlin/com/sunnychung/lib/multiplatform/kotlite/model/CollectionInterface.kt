package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

class CollectionInterface {

    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "Collection",
            typeParameters = listOf(TypeParameterNode(name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
            superClassInvocation = "Iterable<T>()",
            superClass = IterableInterface.clazz,
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        )
    }
}
