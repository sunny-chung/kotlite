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
    typeParameters: List<TypeParameterNode>? = null,
    valueParameters: List<FunctionValueParameterNode>? = null,
    modifiers: Set<FunctionModifier>? = null,
    body: BlockNode? = null,
    transformedRefName: String? = null,
) : FunctionDeclarationNode(
    name = name ?: def.functionName,
    receiver = receiver ?: def.receiverType?.toTypeNode(),
    declaredReturnType = returnType ?: def.returnType.toTypeNode(),
    typeParameters = typeParameters ?: def.typeParameters.map {
        TypeParameterNode(it.name, it.typeUpperBound?.toTypeNode())
    },
    valueParameters = valueParameters ?: def.parameterTypes.map {
        FunctionValueParameterNode(it.name, it.type.toTypeNode(), it.defaultValueExpression?.let { Parser(Lexer(it)).expression() }, it.modifiers)
    },
    modifiers = modifiers ?: def.modifiers,
    body = body ?: BlockNode(emptyList(), SourcePosition(1, 1), ScopeType.Function, FunctionBodyFormat.Block, def.returnType.toTypeNode()),
    transformedRefName = transformedRefName,
) {
    override fun execute(interpreter: Interpreter, receiver: RuntimeValue?, arguments: List<RuntimeValue>, typeArguments: Map<String, DataType>): RuntimeValue {
        return def.executable(receiver, arguments, typeArguments)
    }

    override fun copy(
        name: String,
        receiver: TypeNode?,
        declaredReturnType: TypeNode?,
        typeParameters: List<TypeParameterNode>,
        valueParameters: List<FunctionValueParameterNode>,
        modifiers: Set<FunctionModifier>,
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
            typeParameters = typeParameters,
            valueParameters = valueParameters,
            modifiers = modifiers,
            body = body,
            transformedRefName = transformedRefName
        )
    }
}
