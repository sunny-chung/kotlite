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

        val functions = listOf(
            CustomFunctionDefinition(
                receiverType = "Iterable<T>",
                functionName = "iterator",
                returnType = "Iterator<T>",
                parameterTypes = emptyList(),
                typeParameters = listOf(
                    TypeParameter(name = "T", typeUpperBound = null),
                ),
                executable = { interpreter, receiver, args, typeArgs ->
                    receiver as ListValue
                    IteratorValue(receiver.value.iterator(), typeArgs["T"]!!, interpreter.symbolTable())
                },
                position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
            ),
        )
    }
}
