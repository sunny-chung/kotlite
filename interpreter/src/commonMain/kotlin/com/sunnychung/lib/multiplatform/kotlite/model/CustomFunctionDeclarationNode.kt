package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer

fun String.toTypeNode() = Parser(Lexer(this)).type(isParseDottedIdentifiers = true, isIncludeLastIdentifierAsTypeName = true)

class CustomFunctionDeclarationNode(
    private val def: CustomFunctionDefinition,
    name: String? = null,
    receiver: TypeNode? = null,
    returnType: TypeNode? = null,
    valueParameters: List<FunctionValueParameterNode>? = null,
    body: BlockNode? = null,
    transformedRefName: String? = null,
) : FunctionDeclarationNode(
    name = name ?: def.functionName,
    receiver = receiver ?: def.receiverType?.toTypeNode(),
    declaredReturnType = returnType ?: def.returnType.toTypeNode(),
    valueParameters = valueParameters ?: def.parameterTypes.map {
        FunctionValueParameterNode(it.name, it.type.toTypeNode(), it.defaultValueExpression?.let { Parser(Lexer(it)).expression() })
    },
    body = body ?: BlockNode(emptyList(), SourcePosition(1, 1), ScopeType.Function, FunctionBodyFormat.Block, def.returnType.toTypeNode()),
    transformedRefName = transformedRefName,
) {
    override fun execute(interpreter: Interpreter, receiver: RuntimeValue?, arguments: List<RuntimeValue>): RuntimeValue {
        return def.executable(receiver, arguments)
    }

    override fun copy(
        name: String,
        receiver: TypeNode?,
        declaredReturnType: TypeNode?,
        typeParameters: List<TypeParameterNode>,
        valueParameters: List<FunctionValueParameterNode>,
        body: BlockNode,
        transformedRefName: String?,
        inferredReturnType: TypeNode?,
    ): FunctionDeclarationNode {
        if (this::class != CustomFunctionDeclarationNode::class) {
            throw UnsupportedOperationException("Copying subclasses is not supported")
        }
        return CustomFunctionDeclarationNode(
            def/*.copy(
                receiverType = receiver,
            )*/,
            name = name,
            receiver = receiver,
            returnType = declaredReturnType,
            valueParameters = valueParameters,
            body = body,
            transformedRefName = transformedRefName
        )
    }
}
