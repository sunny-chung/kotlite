package com.sunnychung.lib.multiplatform.kotlite.model

class ComparableInterface {

    companion object {
        val memberFunctions = listOf(
            CustomFunctionDefinition(
                position = SourcePosition.BUILTIN,
                receiverType = null,
                functionName = "compareTo",
                returnType = "Int",
                modifiers = setOf(FunctionModifier.operator, FunctionModifier.open),
                parameterTypes = listOf(CustomFunctionParameter(name = "other", type = "T")),
                executable = exe@ { interpreter, receiver, args, typeArgs ->
                    if (receiver is ComparableRuntimeValue<*> && args[0] is ComparableRuntimeValue<*>) {
                        return@exe IntValue(
                            (receiver as ComparableRuntimeValue<Comparable<Any>>).compareTo(args[0] as ComparableRuntimeValue<Comparable<Any>>),
                            interpreter.symbolTable(),
                        )
                    }
                    throw RuntimeException("${receiver?.type()?.descriptiveName} is not comparable with ${args[0].type().descriptiveName}")
                }
            )
        )

        val interfaze = ProvidedClassDefinition(
            fullQualifiedName = "Comparable",
            isInterface = true,
            typeParameters = listOf(TypeParameterNode(position = SourcePosition.BUILTIN, name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
            functions = memberFunctions.map { it.copy(executable = { _, _, _, _ ->throw UnsupportedOperationException() }) },
            position = SourcePosition.BUILTIN,
        )
    }
}
