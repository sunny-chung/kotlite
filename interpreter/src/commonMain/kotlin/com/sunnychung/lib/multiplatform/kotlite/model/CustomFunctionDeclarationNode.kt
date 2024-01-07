package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter

fun String.toTypeNode() = TypeNode(
    name = this.removeSuffix("?"),
    arguments = null,
    isNullable = this.endsWith("?")
)

class CustomFunctionDeclarationNode(private val def: CustomFunctionDefinition) : FunctionDeclarationNode(
    name = def.functionName,
    receiver = def.receiverType,
    returnType = def.returnType.toTypeNode(),
    valueParameters = def.parameterTypes.map {
        FunctionValueParameterNode(it.first, it.second.toTypeNode(), null)
    },
    body = BlockNode(emptyList(), SourcePosition(1, 1), ScopeType.Function, def.returnType.toTypeNode()),
) {
    override fun execute(interpreter: Interpreter, receiver: RuntimeValue?, arguments: List<RuntimeValue?>): RuntimeValue {
        return def.executable(receiver, arguments)
    }
}
