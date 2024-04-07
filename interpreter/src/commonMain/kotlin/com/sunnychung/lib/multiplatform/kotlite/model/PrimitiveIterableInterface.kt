package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

fun PrimitiveIterableValue(value: Iterable<Any>, typeArgument: DataType, symbolTable: SymbolTable)
    = DelegatedValue(value, IterableInterface.clazz, listOf(typeArgument), symbolTable)

object PrimitiveIterableInterface {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "PrimitiveIterable",
        isInterface = true,
        typeParameters = listOf(TypeParameter(name = "T", typeUpperBound = null)),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("Iterable<T>"),
        position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
    )

    val functions = listOf(
        CustomFunctionDefinition(
            receiverType = "PrimitiveIterable<T>",
            functionName = "iterator",
            returnType = "PrimitiveIterator<T>",
            parameterTypes = emptyList(),
            typeParameters = listOf(
                TypeParameter(name = "T", typeUpperBound = null),
            ),
            executable = { interpreter, receiver, args, typeArgs ->
                val delegatedValue = (receiver as DelegatedValue<*>).value as Iterable<RuntimeValue>
                PrimitiveIteratorValue(delegatedValue.iterator(), typeArgs["T"]!!, interpreter.symbolTable())
            },
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        ),
    )
}
