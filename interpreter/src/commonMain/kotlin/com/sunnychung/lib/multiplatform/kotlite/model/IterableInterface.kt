package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

fun IterableValue(value: Iterable<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable)
    = DelegatedValue<Iterable<RuntimeValue>>(value, IterableInterface.clazz, listOf(typeArgument), symbolTable)

object IterableInterface {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "Iterable",
        isInterface = true,
        typeParameters = listOf(TypeParameter(name = "T", typeUpperBound = null)),
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
                val delegatedValue = (receiver as DelegatedValue<*>).value as Iterable<RuntimeValue>
                IteratorValue(delegatedValue.iterator(), typeArgs["T"]!!, interpreter.symbolTable())
            },
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        ),
    )
}
