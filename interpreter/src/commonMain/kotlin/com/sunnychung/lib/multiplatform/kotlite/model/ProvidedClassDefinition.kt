package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer

class ProvidedClassDefinition(
    fullQualifiedName: String,
    typeParameters: List<TypeParameterNode>,
    isInstanceCreationAllowed: Boolean,
    private val primaryConstructorParameters: List<CustomFunctionParameter>,
    private val constructInstance: (interpreter: Interpreter, callArguments: Array<RuntimeValue>, callPosition: SourcePosition) -> ClassInstance,
) : ClassDefinition(
    currentScope = null,
    name = fullQualifiedName.substringAfterLast('.'),
    fullQualifiedName = fullQualifiedName,
    typeParameters = typeParameters,
    isInstanceCreationAllowed = isInstanceCreationAllowed,
    orderedInitializersAndPropertyDeclarations = emptyList(),
    declarations = emptyList(),
    rawMemberProperties = emptyList(),
    memberFunctions = emptyMap(),
    primaryConstructor = ClassPrimaryConstructorNode(primaryConstructorParameters.map {
        ClassParameterNode(
            isProperty = false,
            isMutable = false,
            parameter = FunctionValueParameterNode(
                name = it.name,
                declaredType = it.type.toTypeNode(),
                defaultValue = it.defaultValueExpression?.let {
                    Parser(Lexer(it)).expression()
                },
                transformedRefName = it.name,
                modifiers = emptySet(),
            )
        )
    })
) {
    override fun construct(
        interpreter: Interpreter,
        callArguments: Array<RuntimeValue>,
        typeArguments: Array<DataType>,
        callPosition: SourcePosition
    ): ClassInstance {
        return constructInstance(interpreter, callArguments, callPosition).also {
            if (!it.hasInitialized) {
                it.attach(this)
            }
        }
    }

    fun copyNullableClassDefinition() = ProvidedClassDefinition(
        fullQualifiedName = "$fullQualifiedName?",
        typeParameters = typeParameters,
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = {_, _, _ -> throw UnsupportedOperationException()},
    )

    fun copyCompanionClassDefinition() = ProvidedClassDefinition(
        fullQualifiedName = "$fullQualifiedName.Companion",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = {_, _, _ -> throw UnsupportedOperationException()},
    )
}
