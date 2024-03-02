package com.sunnychung.lib.multiplatform.kotlite.model

open class ThrowableValue(
    currentScope: SymbolTable,
    val message: String?,
    val cause: ThrowableValue?,
    val stacktrace: List<String>,
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
        )
    }
}

class NullPointerExceptionValue(
    currentScope: SymbolTable,
    message: String? = "null",
    cause: ThrowableValue? = null,
    stacktrace: List<String>,
) : ThrowableValue(
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
            superClassInvocation = "Throwable(message, cause)",
            superClass = ThrowableValue.clazz,
        )
    }
}

class TypeCastExceptionValue(
    currentScope: SymbolTable,
    val valueType: String,
    val targetType: String,
    stacktrace: List<String>,
) : ThrowableValue(
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
            superClassInvocation = "Throwable()",
            superClass = ThrowableValue.clazz,
        )
    }
}
