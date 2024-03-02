package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer

class ProvidedClassDefinition(
    val position: SourcePosition,
    fullQualifiedName: String,
    typeParameters: List<TypeParameterNode>,
    isInstanceCreationAllowed: Boolean,
    private val primaryConstructorParameters: List<CustomFunctionParameter>,
    private val constructInstance: (interpreter: Interpreter, callArguments: Array<RuntimeValue>, callPosition: SourcePosition) -> ClassInstance,
    superClassInvocation: String? = null,
    superClass: ClassDefinition? = null,
) : ClassDefinition(
    currentScope = null,
    name = fullQualifiedName.substringAfterLast('.'),
    fullQualifiedName = fullQualifiedName,
    modifiers = emptySet(),
    typeParameters = typeParameters,
    isInstanceCreationAllowed = isInstanceCreationAllowed,
    orderedInitializersAndPropertyDeclarations = emptyList(),
    declarations = emptyList(),
    rawMemberProperties = emptyList(),
    memberFunctions = emptyMap(),
    primaryConstructor = ClassPrimaryConstructorNode(primaryConstructorParameters.map {
        val modifiers = with(Parser(Lexer("", ""))) { it.modifiers.toClassParameterModifiers() }
        ClassParameterNode(
            isProperty = false,
            isMutable = false,
            modifiers = modifiers.filterIsInstance<PropertyModifier>().toSet(),
            parameter = FunctionValueParameterNode(
                name = it.name,
                declaredType = it.type.toTypeNode(position.filename),
                defaultValue = it.defaultValueExpression?.let {
                    Parser(Lexer(position.filename, it)).expression()
                },
                transformedRefName = it.name,
                modifiers = modifiers.filterIsInstance<FunctionValueParameterModifier>().toSet(),
            )
        )
    }),
    superClassInvocation = superClassInvocation?.let {
        Parser(Lexer(position.filename, it)).delegationSpecifiers()
    },
    superClass = superClass,
) {
    override fun construct(
        interpreter: Interpreter,
        callArguments: Array<RuntimeValue>,
        typeArguments: Array<DataType>,
        callPosition: SourcePosition
    ): ClassInstance {
        return constructInstance(interpreter, callArguments, callPosition).also {
            if (!it.hasInitialized) {
                it.attach(this, interpreter.symbolTable())
            }
        }
    }

    fun copyNullableClassDefinition() = ProvidedClassDefinition(
        fullQualifiedName = "$fullQualifiedName?",
        typeParameters = typeParameters,
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = {_, _, _ -> throw UnsupportedOperationException()},
        position = position,
    )

    fun copyCompanionClassDefinition() = ProvidedClassDefinition(
        fullQualifiedName = "$fullQualifiedName.Companion",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = {_, _, _ -> throw UnsupportedOperationException()},
        position = position,
    )
}
