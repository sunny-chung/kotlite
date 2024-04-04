package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename
import com.sunnychung.lib.multiplatform.kotlite.util.wrapPrimitiveValueAsRuntimeValue

fun PrimitiveIteratorValue(value: Iterator<Any>, typeArgument: DataType, symbolTable: SymbolTable)
    = DelegatedValue<Iterator<Any>>(value, PrimitiveIteratorClass.clazz, listOf(typeArgument), symbolTable)

object PrimitiveIteratorClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "PrimitiveIterator",
        typeParameters = listOf(TypeParameterNode(SourcePosition.BUILTIN, name = "T", typeUpperBound = null)),
        isInterface = true,
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("Iterator<T>"),
        position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
    )

    val functions = listOf(
        CustomFunctionDefinition(
            receiverType = "PrimitiveIterator<T>",
            functionName = "hasNext",
            returnType = "Boolean",
            parameterTypes = emptyList(),
            typeParameters = listOf(
                TypeParameter(name = "T", typeUpperBound = null),
            ),
            modifiers = setOf(FunctionModifier.operator),
            executable = { interpreter, receiver, args, typeArgs ->
                receiver as DelegatedValue<Iterator<Any>>
                BooleanValue(receiver.value.hasNext(), interpreter.symbolTable())
            },
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        ),
        CustomFunctionDefinition(
            receiverType = "PrimitiveIterator<T>",
            functionName = "next",
            returnType = "T",
            parameterTypes = emptyList(),
            typeParameters = listOf(
                TypeParameter(name = "T", typeUpperBound = null),
            ),
            modifiers = setOf(FunctionModifier.operator),
            executable = { interpreter, receiver, args, typeArgs ->
                receiver as DelegatedValue<Iterator<Any>>
                val value = receiver.value.next()
                wrapPrimitiveValueAsRuntimeValue(
                    value = value,
                    type = typeArgs["T"]!!,
                    symbolTable = interpreter.symbolTable()
                )
            },
            position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
        ),
    )
}

