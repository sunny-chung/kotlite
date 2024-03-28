package com.sunnychung.lib.multiplatform.kotlite.model

class AnyClass {

    companion object {
        val memberFunctions = listOf(
            CustomFunctionDefinition(
                position = SourcePosition.BUILTIN,
                receiverType = null,
                functionName = "equals",
                returnType = "Boolean",
                // Intentionally drop "operator" modifier to lessen performance penalty
                // Otherwise, it won't pass LoopTest.
                modifiers = setOf(/*FunctionModifier.operator,*/ FunctionModifier.open),
                parameterTypes = listOf(CustomFunctionParameter(name = "other", type = "Any?")),
                executable = exe@ { interpreter, receiver, args, typeArgs ->
                    val other = args[0]
                    if (receiver is ClassInstance) { // prevent infinite loop of `equals()` calls
                        BooleanValue(receiver === other, interpreter.symbolTable())
                    } else {
                        BooleanValue(receiver == other, interpreter.symbolTable())
                    }
                }
            ),
            CustomFunctionDefinition(
                position = SourcePosition.BUILTIN,
                receiverType = null,
                functionName = "hashCode",
                returnType = "Int",
                modifiers = setOf(FunctionModifier.open),
                parameterTypes = emptyList(),
                executable = exe@ { interpreter, receiver, args, typeArgs ->
                    if (receiver is ClassInstance) { // prevent infinite loop
                        IntValue(receiver.originalHashCode(), interpreter.symbolTable())
                    } else {
                        IntValue(receiver.hashCode(), interpreter.symbolTable())
                    }
                }
            ),
            CustomFunctionDefinition(
                position = SourcePosition.BUILTIN,
                receiverType = null,
                functionName = "toString",
                returnType = "String",
                modifiers = setOf(FunctionModifier.open),
                parameterTypes = emptyList(),
                executable = exe@ { interpreter, receiver, args, typeArgs ->
                    StringValue(receiver?.convertToString() ?: "null", interpreter.symbolTable())
                }
            ),
        )

        val clazz: ProvidedClassDefinition = ProvidedClassDefinition(
            fullQualifiedName = "Any",
            typeParameters = emptyList(),
            modifiers = setOf(ClassModifier.open),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = emptyList(),
            constructInstance = { interpreter, _, _ -> ClassInstance(
                currentScope = interpreter.symbolTable(),
                fullClassName = "Any",
                clazz = interpreter.symbolTable().findClass("Any")!!.first,
                typeArguments = emptyList(),
            ) },
            functions = memberFunctions,
            position = SourcePosition.BUILTIN,
        )
    }
}
