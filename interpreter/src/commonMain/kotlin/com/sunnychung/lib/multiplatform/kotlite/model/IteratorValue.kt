package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

fun IteratorValue(value: Iterator<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable)
    = DelegatedValue<Iterator<RuntimeValue>>(value, IteratorClass.clazz, listOf(typeArgument), symbolTable)

object IteratorClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "Iterator",
        typeParameters = listOf(TypeParameter(name = "T", typeUpperBound = null)),
        isInterface = true,
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
    )

    val functions = listOf(
        CustomFunctionDefinition(
            receiverType = "Iterator<T>",
            functionName = "hasNext",
            returnType = "Boolean",
            parameterTypes = emptyList(),
            typeParameters = listOf(
                TypeParameter(name = "T", typeUpperBound = null),
            ),
            modifiers = setOf(FunctionModifier.operator),
            executable = { interpreter, receiver, args, typeArgs ->
                receiver as DelegatedValue<Iterator<RuntimeValue>>
                BooleanValue(receiver.value.hasNext(), interpreter.symbolTable())
            },
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        ),
        CustomFunctionDefinition(
            receiverType = "Iterator<T>",
            functionName = "next",
            returnType = "T",
            parameterTypes = emptyList(),
            typeParameters = listOf(
                TypeParameter(name = "T", typeUpperBound = null),
            ),
            modifiers = setOf(FunctionModifier.operator),
            executable = { interpreter, receiver, args, typeArgs ->
                receiver as DelegatedValue<Iterator<RuntimeValue>>
                receiver.value.next()
            },
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        ),
    )
}

