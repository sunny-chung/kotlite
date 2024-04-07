package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer

/**
 * Make a copy of this class before use.
 */
class ProvidedClassDefinition(
    val position: SourcePosition,
    fullQualifiedName: String,
    isInterface: Boolean = false,
    typeParameters: List<TypeParameter>,
    isInstanceCreationAllowed: Boolean,
    private val primaryConstructorParameters: List<CustomFunctionParameter>,
    private val constructInstance: (interpreter: Interpreter, callArguments: Array<RuntimeValue>, callPosition: SourcePosition) -> ClassInstance,
    modifiers: Set<ClassModifier> = emptySet(),
    private val superClassInvocationString: String? = null,
//    superClass: ClassDefinition? = null,
    private val superInterfaceTypeNames: List<String> = emptyList(),
//    superInterfaces: List<ClassDefinition> = emptyList(),
    private val functions: List<CustomFunctionDefinition> = emptyList(),
) : ClassDefinition(
    currentScope = null,
    name = fullQualifiedName.substringAfterLast('.'),
    fullQualifiedName = fullQualifiedName,
    isInterface = isInterface,
    modifiers = modifiers,
    typeParameters = typeParameters.toTypeParameterNodes(position = position),
    isInstanceCreationAllowed = isInstanceCreationAllowed,
    orderedInitializersAndPropertyDeclarations = emptyList(),
    declarations = emptyList(),
    rawMemberProperties = emptyList(),
    memberFunctions = functions.map { CustomFunctionDeclarationNode(it) },
    primaryConstructor = ClassPrimaryConstructorNode(
        position = position,
        parameters = primaryConstructorParameters.map {
            val modifiers = with(Parser(Lexer("", ""))) { it.modifiers.toClassParameterModifiers() }
            ClassParameterNode(
                position = position,
                isProperty = false,
                isMutable = false,
                modifiers = modifiers.filterIsInstance<PropertyModifier>().toSet(),
                parameter = FunctionValueParameterNode(
                    position = position,
                    name = it.name,
                    declaredType = it.type.toTypeNode(position.filename),
                    defaultValue = it.defaultValueExpression?.let {
                        Parser(Lexer(position.filename, it)).expression()
                    },
                    transformedRefName = it.name,
                    modifiers = modifiers.filterIsInstance<FunctionValueParameterModifier>().toSet(),
                )
            )
        }
    ),
    superClassInvocation = superClassInvocationString?.let {
        Parser(Lexer(position.filename, it)).delegationSpecifiers().single() as? FunctionCallNode
            ?: throw SemanticException(position, "Missing value parameters in super class invocation")
    },
    superInterfaceTypes = superInterfaceTypeNames.map {
        Parser(Lexer(position.filename, it)).typeReference()
    },
) {
    val typeParameters_ = typeParameters

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

    fun copyClassDefinition() = ProvidedClassDefinition(
        fullQualifiedName = fullQualifiedName,
        typeParameters = typeParameters_,
        isInterface = isInterface,
        isInstanceCreationAllowed = isInstanceCreationAllowed,
        primaryConstructorParameters = primaryConstructorParameters,
        constructInstance = constructInstance,
        position = position,
        modifiers = modifiers,
        superClassInvocationString = superClassInvocationString,
        superInterfaceTypeNames = superInterfaceTypeNames,
        functions = functions,
    )

    fun copyNullableClassDefinition() = ProvidedClassDefinition(
        fullQualifiedName = "$fullQualifiedName?",
        typeParameters = typeParameters_,
        isInterface = isInterface,
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
