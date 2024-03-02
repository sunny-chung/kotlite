package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

class IterableInterface {

    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "Iterable",
            typeParameters = listOf(TypeParameterNode(SourcePosition.BUILTIN, name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        )
    }
}
