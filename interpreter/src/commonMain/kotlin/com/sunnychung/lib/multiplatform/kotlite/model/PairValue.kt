package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

fun PairValue(value: Pair<RuntimeValue, RuntimeValue>, typeA: DataType, typeB: DataType, symbolTable: SymbolTable) : DelegatedValue<Pair<RuntimeValue, RuntimeValue>>
    = DelegatedValue<Pair<RuntimeValue, RuntimeValue>>(value, PairClass.clazz, listOf(typeA, typeB), symbolTable)

object PairClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "Pair",
        typeParameters = listOf(
            TypeParameter(name = "A", typeUpperBound = null),
            TypeParameter(name = "B", typeUpperBound = null)
        ),
        isInstanceCreationAllowed = true,
        primaryConstructorParameters = listOf(
            CustomFunctionParameter("first", "A"),
            CustomFunctionParameter("second", "B"),
        ),
        constructInstance = { interpreter, callArguments, callPosition ->
            PairValue(
                Pair(callArguments[0], callArguments[1]),
                callArguments[0].type(), // TODO
                callArguments[1].type(), // TODO
                interpreter.symbolTable(),
            )
        },
        position = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
    )

    val properties = listOf(Property.first, Property.second)

    object Property {
        val first = ExtensionProperty(
            "first",
            listOf(
                TypeParameter("A", null),
                TypeParameter("B", null),
            ),
            "Pair<A, B>",
            "A",
            getter = { interpreter, receiver, typeArgs ->
                (receiver as DelegatedValue<Pair<RuntimeValue, RuntimeValue>>).value.first
            },
        )

        val second = ExtensionProperty(
            "second",
            listOf(
                TypeParameter("A", null),
                TypeParameter("B", null),
            ),
            "Pair<A, B>",
            "B",
            getter = { interpreter, receiver, typeArgs ->
                (receiver as DelegatedValue<Pair<RuntimeValue, RuntimeValue>>).value.second
            },
        )
    }
}
