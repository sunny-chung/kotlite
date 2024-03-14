package com.sunnychung.lib.multiplatform.kotlite.model

/**
 * The difference between Exception and Throwable is that, Throwable includes exceptions thrown outside the interpreter
 * scope.
 */
open class ThrowableValue(
    currentScope: SymbolTable,
    val message: String?,
    val cause: ThrowableValue?,
    val stacktrace: List<String>,
    val externalExceptionClassName: String? = null,
    thisClazz: ClassDefinition? = null,
    parentInstance: ClassInstance? = null,
) : ClassInstance(
    currentScope = currentScope,
    fullClassName = (thisClazz ?: clazz).fullQualifiedName,
    clazz = thisClazz ?: clazz,
    typeArguments = emptyList(),
    parentInstance = parentInstance,
) {
    companion object {
        val clazz = ProvidedClassDefinition(
            position = SourcePosition.BUILTIN,
            fullQualifiedName = "Throwable",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(
                CustomFunctionParameter("message", "String?", "null"),
                CustomFunctionParameter("cause", "Throwable?", "null"),
            ),
            constructInstance = { interpreter, callArguments, callPosition ->
                val message = (callArguments[0] as? StringValue)?.value
                val cause = callArguments[1] as? ThrowableValue
                ThrowableValue(interpreter.symbolTable(), message, cause, interpreter.callStack.getStacktrace())
            },
            modifiers = setOf(ClassModifier.open),
        )

        val properties = listOf(
            ExtensionProperty(
                declaredName = "message",
                typeParameters = emptyList(),
                receiver = "Throwable",
                type = "String?",
                getter = { interpreter, receiver, typeArgs ->
                    (receiver as ThrowableValue).message?.let { StringValue(it, interpreter.symbolTable()) } ?: NullValue
                },
            ),
            ExtensionProperty(
                declaredName = "cause",
                typeParameters = emptyList(),
                receiver = "Throwable",
                type = "Throwable?",
                getter = { interpreter, receiver, typeArgs ->
                    (receiver as ThrowableValue).cause ?: NullValue
                },
            ),
            ExtensionProperty(
                declaredName = "name",
                typeParameters = emptyList(),
                receiver = "Throwable",
                type = "String",
                getter = { interpreter, receiver, typeArgs ->
                    val value = receiver as ThrowableValue
                    StringValue(value.externalExceptionClassName ?: value.fullClassName, interpreter.symbolTable())
                },
            ),
        )

        val functions = listOf(
            CustomFunctionDefinition(
                position = SourcePosition.BUILTIN,
                receiverType = "Throwable",
                functionName = "stackTraceToString",
                returnType = "String",
                parameterTypes = emptyList(),
                executable = { interpreter, receiver, args, typeArgs ->
                    (receiver as ThrowableValue).stacktrace.joinToString("\n").let { StringValue(it, interpreter.symbolTable()) }
                },
            ),
        )
    }
}

/**
 * The difference between Exception and Throwable is that, Throwable includes exceptions thrown outside the interpreter
 * scope.
 */
open class ExceptionValue(
    currentScope: SymbolTable,
    message: String?,
    cause: ThrowableValue? = null,
    stacktrace: List<String>,
    thisClazz: ClassDefinition? = null,
    parentInstance: ClassInstance? = null,
) : ThrowableValue(
    currentScope = currentScope,
    message = message,
    cause = cause,
    stacktrace = stacktrace,
    thisClazz = thisClazz ?: clazz,
    parentInstance = null,
) {
    companion object {
        val clazz = ProvidedClassDefinition(
            position = SourcePosition.BUILTIN,
            fullQualifiedName = "Exception",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(
                CustomFunctionParameter("message", "String?", "null"),
                CustomFunctionParameter("cause", "Throwable?", "null"),
            ),
            constructInstance = { interpreter, callArguments, callPosition ->
                val message = (callArguments[0] as? StringValue)?.value
                val cause = callArguments[1] as? ThrowableValue
                ExceptionValue(interpreter.symbolTable(), message, cause, interpreter.callStack.getStacktrace())
            },
            superClassInvocationString = "Throwable(message, cause)",
//            superClass = ThrowableValue.clazz,
            modifiers = setOf(ClassModifier.open),
        )
    }
}

class NullPointerExceptionValue(
    currentScope: SymbolTable,
    message: String? = "null",
    cause: ThrowableValue? = null,
    stacktrace: List<String>,
) : ExceptionValue(
    currentScope = currentScope,
    message = message,
    cause = cause,
    stacktrace = stacktrace,
    thisClazz = clazz,
    parentInstance = null,
) {
    companion object {
        val clazz = ProvidedClassDefinition(
            position = SourcePosition.BUILTIN,
            fullQualifiedName = "NullPointerException",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(
                CustomFunctionParameter("message", "String?", "null"),
                CustomFunctionParameter("cause", "Throwable?", "null"),
            ),
            constructInstance = { interpreter, callArguments, callPosition ->
                val message = (callArguments[0] as? StringValue)?.value
                val cause = callArguments[1] as? ThrowableValue
                NullPointerExceptionValue(interpreter.symbolTable(), message, cause, interpreter.callStack.getStacktrace())
            },
            superClassInvocationString = "Exception(message, cause)",
//            superClass = ExceptionValue.clazz,
        )
    }
}

class TypeCastExceptionValue(
    currentScope: SymbolTable,
    val valueType: String,
    val targetType: String,
    stacktrace: List<String>,
) : ExceptionValue(
    currentScope = currentScope,
    message = "`$valueType` cannot be casted to type `$targetType`",
    cause = null,
    stacktrace = stacktrace,
    thisClazz = clazz,
    parentInstance = null,
) {
    companion object {
        val clazz = ProvidedClassDefinition(
            position = SourcePosition.BUILTIN,
            fullQualifiedName = "TypeCastException",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = true,
            primaryConstructorParameters = listOf(
                CustomFunctionParameter("valueType", "String", null),
                CustomFunctionParameter("targetType", "String", null),
            ),
            constructInstance = { interpreter, callArguments, callPosition ->
                val valueType = (callArguments[0] as StringValue).value
                val targetType = (callArguments[1] as StringValue).value
                TypeCastExceptionValue(interpreter.symbolTable(), valueType, targetType, interpreter.callStack.getStacktrace())
            },
            superClassInvocationString = "Exception()",
//            superClass = ExceptionValue.clazz,
        )
    }
}
