package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.extension.emptyToNull
import com.sunnychung.lib.multiplatform.kotlite.extension.mergeIfNotExists
import com.sunnychung.lib.multiplatform.kotlite.util.ClassMemberResolver

/**
 * This class is stateful and may not survive after Semantic Analyzer.
 */
open class ClassDefinition(
    /**
     * For storing parsed declarations
     */
    internal val currentScope: SymbolTable?,

    val name: String,
    val fullQualifiedName: String = name, // TODO

    val isInterface: Boolean = false,

    /**
     * If it is an object class, no new instance can be created
     */
    val isInstanceCreationAllowed: Boolean,
    val modifiers: Set<ClassModifier>,

    val typeParameters: List<TypeParameterNode>,

    /**
     * Only contains ClassInstanceInitializerNode and PropertyDeclarationNode
     */
    val orderedInitializersAndPropertyDeclarations: List<ASTNode>,

    val declarations: List<ASTNode>,

    rawMemberProperties: List<PropertyDeclarationNode>,
    private val memberFunctions: List<FunctionDeclarationNode>,

    val primaryConstructor: ClassPrimaryConstructorNode?,
    val superInterfaceTypes: List<TypeNode> = emptyList(),
    superClassInvocation: FunctionCallNode? = null,
    var superClass: ClassDefinition? = null,
    var superInterfaces: List<ClassDefinition> = emptyList(),
) {
    private val declaredSuperClassInvocation = superClassInvocation
    var superClassInvocation: FunctionCallNode? = null

    private fun initSuperClass(symbolTable: SymbolTable, semanticAnalyzer: SemanticAnalyzer?) {
        if (
            superClass == null
            && declaredSuperClassInvocation == null
            && !isInterface
            && fullQualifiedName != "Any"
            && !fullQualifiedName.endsWith("?")
        ) {
//            superClass = (symbolTable.findClass("Any") ?: throw RuntimeException("Class `Any` not found")).first
            this.superClassInvocation = FunctionCallNode(
                function = TypeNode(
                    position = SourcePosition.BUILTIN,
                    name = "Any",
                    arguments = null,
                    isNullable = false,
                ),
                arguments = emptyList(),
                declaredTypeArguments = emptyList(),
                position = SourcePosition.BUILTIN,
                isSuperclassConstruction = true,
                callableType = CallableType.Constructor,
                functionRefName = "Any",
                returnType = TypeNode( // TypeNode cannot be reused
                    position = SourcePosition.BUILTIN,
                    name = "Any",
                    arguments = null,
                    isNullable = false,
                ),
            ).also {
                with (semanticAnalyzer ?: return@also) {
                    it.visit(isSuperClassInvocation = true)
                }
            }
            index = findIndex() + 1 // a super class is added, so tree index increases by 1
        } else if (this.superClassInvocation == null) {
            this.superClassInvocation = declaredSuperClassInvocation
        }
    }

    var enumValues: Map<String, ClassInstance> = emptyMap()

    internal var isInInterpreter = false
    internal var memberFunctionsForInterpreter: Map<String, FunctionDeclarationNode>? = null
    internal var memberFunctionsForSA: Map<String, FunctionDeclarationNode>? = null
    val memberFunctionsMap: Map<String, FunctionDeclarationNode>
        get() = if (isInInterpreter) {
            memberFunctionsForInterpreter ?: throw RuntimeException("memberFunctionsForInterpreter not initialized for type $fullQualifiedName")
        } else {
            memberFunctionsForSA ?: throw RuntimeException("memberFunctionsForSA not initialized for type $fullQualifiedName")
        }

    protected val specialFunctions = mutableMapOf<SpecialFunction.Name, SpecialFunction>()

    // only used if the class extends `Comparable<*>`
    var compareToFunctionNode: FunctionDeclarationNode? = null
    var compareToExec: ((subject: RuntimeValue, other: RuntimeValue) -> Int)? = null

    fun getSpecialFunction(name: SpecialFunction.Name): SpecialFunction? {
        return specialFunctions[name]
    }

    fun validateSuperClassesAndInterfaces() {
        if (isInterface) {
            if (superClass != null || superClassInvocation != null) {
                throw SemanticException(SourcePosition.NONE, "Interface cannot extend from a class")
            }
        }

        if (superClass != null && superClassInvocation == null) {
            throw SemanticException(SourcePosition.NONE, "superClassInvocation for class `$fullQualifiedName` must be provided if there is a super class")
        } else if (superClass == null && superClassInvocation != null) {
            throw SemanticException(SourcePosition.NONE, "superClass must be provided if there is a super class invocation")
        } else if (superClass != null && superClassInvocation != null) {
            if (superClass!!.fullQualifiedName != (superClassInvocation!!.function as TypeNode).name) {
                throw SemanticException(SourcePosition.NONE, "superClass and superClassInvocation do not match -- ${superClass!!.fullQualifiedName} VS ${(superClassInvocation!!.function as TypeNode).name}")
            }
        }

        superInterfaces.forEach { def ->
            superInterfaceTypes.singleOrNull { it.name in setOf(def.name, def.fullQualifiedName) }
                ?: throw SemanticException(SourcePosition.NONE, "Missing or repeated superInterfaceTypes on the super interface type ${def.fullQualifiedName}")
        }

        superInterfaceTypes.forEach { type ->
            superInterfaces.singleOrNull { it.fullQualifiedName == type.name }
                ?: throw SemanticException(SourcePosition.NONE, "Missing or repeated superInterfaces on the super interface type ${type.name}")
        }

        if (superClass != null && superClass!!.isInterface) {
            throw SemanticException(SourcePosition.NONE, "superClass ${superClass!!.fullQualifiedName} is not a class but an interface")
        }

        superInterfaces.forEach { def ->
            if (!def.isInterface) {
                throw SemanticException(SourcePosition.NONE, "superInterfaces ${def.fullQualifiedName} is not an interface but a class")
            }
        }
    }

    // key = original name
    // does not include properties with custom accessors
    private val memberProperties: Map<String, PropertyType> = mutableMapOf()
    // key = original name
    private val memberPropertyTypes: Map<String, PropertyType> = mutableMapOf()
    // key = original name
    private val memberPropertyCustomAccessors: Map<String, PropertyAccessorsNode> = mutableMapOf()
    private val memberTransformedNameToPropertyName: Map<String, String> = mutableMapOf()
    private val memberPropertyNameToTransformedName: Map<String, String> = mutableMapOf()

    private fun findIndex(): Int {
        if (superClass == null) return 0
        return superClass!!.findIndex() + 1
    }

    var index = findIndex()
        private set

    val isRealClass: Boolean
        get() = !isInterface
                && fullQualifiedName !in setOf("Function", "Function?")
                && !fullQualifiedName.endsWith("?")
                && !fullQualifiedName.endsWith(".Companion")

    init {
        rawMemberProperties.forEach { addProperty(currentScope, it) }
    }

    fun copyAsEmptyClass() = ClassDefinition(
        currentScope = currentScope,
        name = name,
        fullQualifiedName = fullQualifiedName,
        isInterface = isInterface,
        modifiers = modifiers,
        typeParameters = typeParameters,
        isInstanceCreationAllowed = isInstanceCreationAllowed,
        primaryConstructor = primaryConstructor,
        rawMemberProperties = emptyList(),
        memberFunctions = emptyList(),
        orderedInitializersAndPropertyDeclarations = emptyList(),
        declarations = emptyList(),
        superClassInvocation = superClassInvocation,
        superClass = superClass,
        superInterfaceTypes = superInterfaceTypes,
        superInterfaces = superInterfaces,
    ).also {
        it.memberFunctionsForSA = emptyMap()
        it.superClassInvocation = it.declaredSuperClassInvocation
    }

    /**
     * Calls to this function should be after the class is registered to `currentScope`
     */
    fun attachToSemanticAnalyzer(sa: SemanticAnalyzer, isReady: Boolean = true) {
        initSuperClass(sa.currentScope, sa)

        superClassInvocation?.function?.let { it as? TypeNode }?.name
            ?.also {
                superClass = sa.currentScope.findClass(it)?.first
                    ?: throw SemanticException(SourcePosition.NONE, "Cannot find class $it")
            }

        superInterfaces = superInterfaceTypes.map { sa.currentScope.findClass(it.name)?.first ?: throw SemanticException(SourcePosition.NONE, "Cannot find interface $it") }

        validateSuperClassesAndInterfaces()

        val classMemberResolver = ClassMemberResolver.create(sa.currentScope, copyAsEmptyClass(), null)
        memberFunctionsForSA = memberFunctions.onEach { thisFunc ->
            with (sa) {
                // TODO these are duplicating with ClassSemanticAnalyzer and SemanticAnalyzer. Refactor these
                val superClassFunctions = classMemberResolver?.findMemberFunctionsAndExactTypesByDeclaredName(thisFunc.name) ?: emptyMap()
                var hasOverridden = false
                val identicalSuperClassFunctions = superClassFunctions.filter {
                    it.value.resolvedValueParameterTypes.size == thisFunc.valueParameters.size
                        && it.value.resolvedValueParameterTypes.withIndex().all {
                            it.value.type == thisFunc.valueParameters[it.index].type
                        }
                }.values
                identicalSuperClassFunctions.forEach { superFunc ->
                    if (FunctionModifier.open !in superFunc.function.modifiers) {
                        throw SemanticException(thisFunc.position, "A function cannot override another function not marked as `open`")
                    }
                    superFunc.function.transformedRefName?.let {
                        thisFunc.transformedRefName = it // reuse parent's transformedRefName to have less trouble in implementation
                    }
                }
                hasOverridden = identicalSuperClassFunctions.isNotEmpty()
                if (hasOverridden && FunctionModifier.override !in thisFunc.modifiers) {
                    throw SemanticException(thisFunc.position, "A function cannot override anything without the modifier `override`")
                }
                if (!hasOverridden && FunctionModifier.override in thisFunc.modifiers) {
                    throw SemanticException(thisFunc.position, "Function `${thisFunc.name}` of type `$fullQualifiedName` overrides nothing")
                }
                // --- end duplication

                if (thisFunc.transformedRefName == null) {
                    thisFunc.transformedRefName = thisFunc.toSignature(currentScope)
                }
                sa.executionEnvironment.registerGeneratedMapping(
                    type = ExecutionEnvironment.SymbolType.Function,
                    receiverType = fullQualifiedName,
                    name = thisFunc.name,
                    transformedName = thisFunc.transformedRefName!!,
                )

                thisFunc.valueParameters.forEach { p ->
                    if (p.transformedRefName == null) {
                        p.generateTransformedName()
                        executionEnvironment.registerGeneratedMapping(
                            type = ExecutionEnvironment.SymbolType.ValueParameter,
                            receiverType = fullQualifiedName,
                            parentName = thisFunc.name,
                            name = p.name,
                            transformedName = p.transformedRefName!!,
                        )
                    }
                }
            }
        }.associateBy { it.toSignature(sa.currentScope) }

        val symbolTable = sa.currentScope
        if (isReady && !isInterface && classMemberResolver?.containsSuperType("Comparable") == true) {
            val reference = classMemberResolver.genericResolutionsByTypeName["Comparable"]!!
            var callables = sa.currentScope.findMatchingCallables(
                currentSymbolTable = sa.currentScope,
                originalName = "compareTo",
                receiverType = symbolTable.assertToDataType(
                    TypeNode(
                        position = SourcePosition.NONE,
                        name = fullQualifiedName,
                        arguments = typeParameters.map {
                            TypeNode(SourcePosition.NONE, it.name, null, false)
                        }.emptyToNull(),
                        isNullable = false
                    )
                ),
                arguments = listOf(FunctionCallArgumentInfo(null, symbolTable.assertToDataType(reference.values.first()))),
                modifierFilter = SearchFunctionModifier.OperatorFunctionOnly, // prefer operator (extension) functions over normal member functions
            )
            if (callables.isEmpty()) {
                callables = sa.currentScope.findMatchingCallables(
                    currentSymbolTable = sa.currentScope,
                    originalName = "compareTo",
                    receiverType = symbolTable.assertToDataType(
                        TypeNode(
                            position = SourcePosition.NONE,
                            name = fullQualifiedName,
                            arguments = typeParameters.map {
                                TypeNode(SourcePosition.NONE, it.name, null, false)
                            }.emptyToNull(),
                            isNullable = false
                        )
                    ),
                    arguments = listOf(FunctionCallArgumentInfo(null, symbolTable.assertToDataType(reference.values.first()))),
                    modifierFilter = SearchFunctionModifier.NoRestriction,
                )
            }
            if (callables.isEmpty()) {
                throw SemanticException(SourcePosition.NONE, "`compareTo` function not found")
            } else if (callables.size > 1) {
                throw SemanticException(SourcePosition.NONE, "Ambiguous `compareTo` functions")
            }
            compareToFunctionNode = callables.single().definition as FunctionDeclarationNode
            sa.executionEnvironment.registerSpecialFunction(
                type = ExecutionEnvironment.SymbolType.Function,
                receiverType = fullQualifiedName,
                name = "compareTo",
                function = compareToFunctionNode!!,
            )
        }

        if (isReady && isRealClass) {
            symbolTable.declareTempTypeAlias(typeParameters.map { it.name to it.typeUpperBoundOrAny() })
            SpecialFunction.Name.entries.forEach { specialFunction ->
                var callables: List<FindCallableResult> = emptyList()

                specialFunction.acceptableValueParameterTypes.forEach { valueParameters ->
                    if (callables.isEmpty()) {
                        callables = sa.currentScope.findMatchingCallables(
                            currentSymbolTable = sa.currentScope,
                            originalName = specialFunction.functionName,
                            receiverType = symbolTable.assertToDataType(
                                TypeNode(
                                    position = SourcePosition.NONE,
                                    name = fullQualifiedName,
                                    arguments = typeParameters.map {
                                        TypeNode(SourcePosition.NONE, it.name, null, false)
                                    }.emptyToNull(),
                                    isNullable = false
                                )
                            ),
                            arguments = valueParameters.map {
                                FunctionCallArgumentInfo(
                                    null,
                                    symbolTable.assertToDataType(it)
                                )
                            },
                            modifierFilter = SearchFunctionModifier.NoRestriction,
                        )
                            .filter { it.receiverType?.name != "Any" }
                    }
                    if (callables.size > 1) {
                        throw SemanticException(SourcePosition.NONE, "Ambiguous `${specialFunction.functionName}` functions")
                    }
                }
                if (callables.isNotEmpty()) {
                    val function = callables.single().definition as FunctionDeclarationNode
                    specialFunctions[specialFunction] = SpecialFunction(function)
                    sa.executionEnvironment.registerSpecialFunction(
                        type = ExecutionEnvironment.SymbolType.Function,
                        receiverType = fullQualifiedName,
                        name = specialFunction.functionName,
                        function = function,
                    )
                }
            }
            symbolTable.popTempTypeAlias()
        }
    }

    fun attachToInterpreter(interpreter: Interpreter) {
        isInInterpreter = true

        initSuperClass(interpreter.symbolTable(), null)

        superClassInvocation?.function?.let { it as? TypeNode }?.name
            ?.also { superClass = interpreter.symbolTable().findClass(it)?.first ?: throw SemanticException(SourcePosition.NONE, "Cannot find class $it") }

        superInterfaces = superInterfaceTypes.map { interpreter.symbolTable().findClass(it.name)?.first ?: throw SemanticException(SourcePosition.NONE, "Cannot find interface $it") }

        memberFunctionsForInterpreter = memberFunctions.onEach {
            with(interpreter) {
                it.valueParameters.forEach { p ->
                    if (p.transformedRefName == null) {
                        p.transformedRefName = executionEnvironment.findGeneratedMapping(
                            type = ExecutionEnvironment.SymbolType.ValueParameter,
                            receiverType = fullQualifiedName,
                            parentName = it.name,
                            name = p.name,
                        ).transformedName
                    }
                }
                if (it.transformedRefName == null) {
                    it.transformedRefName = executionEnvironment.findGeneratedMapping(
                        type = ExecutionEnvironment.SymbolType.Function,
                        receiverType = fullQualifiedName,
                        name = it.name,
                    ).transformedName
                }
            }
        }.associateBy { it.transformedRefName!! }

        val classMemberResolver = ClassMemberResolver.create(interpreter.symbolTable(), copyAsEmptyClass(), null)

        if (compareToFunctionNode == null && !isInterface && classMemberResolver?.containsSuperType("Comparable") == true) {
            compareToFunctionNode = interpreter.executionEnvironment.findSpecialFunction(
                type = ExecutionEnvironment.SymbolType.Function,
                receiverType = fullQualifiedName,
                name = "compareTo",
            )
        }

        compareToExec = { subject, other ->
            with(interpreter) {
                val function = compareToFunctionNode ?: throw RuntimeException("`compareTo` function not found for type ${subject.type().descriptiveName}")
                FunctionCallNode(
                    function = function,
                    arguments = listOf(
                        FunctionCallArgumentNode(
                            position = SourcePosition.NONE,
                            index = 0,
                            value = ValueNode(SourcePosition.NONE, other)
                        )
                    ),
                    declaredTypeArguments = emptyList(),
                    position = SourcePosition("", 1, 1)
                ).evalClassMemberAnyFunctionCall(subject, function)
                    .let { (it as IntValue).value }
            }
        }

        if (!isInterface && isRealClass) {
            SpecialFunction.Name.entries.forEach { specialFunction ->
                interpreter.executionEnvironment.findNullableSpecialFunction(
                    type = ExecutionEnvironment.SymbolType.Function,
                    receiverType = fullQualifiedName,
                    name = specialFunction.functionName,
                )?.let { func ->
                    specialFunctions[specialFunction] = SpecialFunction(func)
                }
            }
        }
    }

    fun isInstanceCreationByUserAllowed() = isInstanceCreationAllowed && ClassModifier.abstract !in modifiers

    /**
     * Only for SemanticAnalyzer use during parsing class declarations.
     */
    internal fun addProperty(currentScope: SymbolTable?, it: PropertyDeclarationNode) {
        if (isInterface) {
            throw RuntimeException("Properties in interfaces are not supported")
        }

        val type = (currentScope!!.typeNodeToPropertyType(
            it.type,
            it.isMutable
        ) ?: if (it.type.name in setOf(name, fullQualifiedName)) {
            val type = currentScope!!.resolveObjectType(this, this.typeParameters.map { TypeNode(it.position, it.name, null, false) }, it.type.isNullable, upToIndex = index - 1)
            PropertyType(type!!, it.isMutable)
//            PropertyType(ObjectType(this, it.type.arguments?.map { currentScope!!.typeNodeToDataType(it)!! } ?: emptyList(), it.type.isNullable, superType), it.isMutable)
//            PropertyType(ObjectType(this, it.type.arguments?.map { currentScope!!.typeNodeToDataType(it)!! } ?: emptyList(), it.type.isNullable), it.isMutable)
        } else throw RuntimeException("Unknown type ${it.type.name}"))
        (memberPropertyTypes as MutableMap)[it.name] = type
        if (it.accessors == null) {
            (memberProperties as MutableMap)[it.name] = type
        } else {
            (memberPropertyCustomAccessors as MutableMap)[it.name] = it.accessors
        }

        if (it.transformedRefName != null) {
            (memberTransformedNameToPropertyName as MutableMap)[it.transformedRefName!!] = it.name
            (memberPropertyNameToTransformedName as MutableMap)[it.name] = it.transformedRefName!!
        }
    }


    /**
     * Key: Original declared name
     */
    fun getAllMemberPropertiesExcludingCustomAccessors(): Map<String, PropertyType> {
        return memberProperties mergeIfNotExists (superClass?.getAllMemberPropertiesExcludingCustomAccessors() ?: emptyMap())
    }

    /**
     * Key: Original declared name
     */
    fun getAllMemberProperties(): Map<String, PropertyType> {
        return memberPropertyTypes mergeIfNotExists (superClass?.getAllMemberProperties() ?: emptyMap())
    }

    fun getDeclaredPropertiesInThisClass() = memberProperties
    fun getDeclaredPropertyAccessorsInThisClass() = memberPropertyCustomAccessors

    fun findMemberPropertyWithIndex(declaredName: String, inThisClassOnly: Boolean = false) : Pair<PropertyType, Int>? =
        memberPropertyTypes[declaredName]?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyWithIndex(declaredName, inThisClassOnly) }

    fun findMemberProperty(declaredName: String, inThisClassOnly: Boolean = false) =
        findMemberPropertyWithIndex(declaredName, inThisClassOnly)?.first

    fun findMemberPropertyWithoutAccessorWithIndex(declaredName: String, inThisClassOnly: Boolean = false): Pair<PropertyType, Int>? =
        memberProperties[declaredName]?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyWithoutAccessorWithIndex(declaredName, inThisClassOnly) }

    fun findMemberPropertyWithoutAccessor(declaredName: String, inThisClassOnly: Boolean = false) =
        findMemberPropertyWithoutAccessorWithIndex(declaredName, inThisClassOnly)?.first

    fun findMemberPropertyCustomAccessorWithIndex(declaredName: String, inThisClassOnly: Boolean = false): Pair<PropertyAccessorsNode, Int>? =
        memberPropertyCustomAccessors[declaredName]?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyCustomAccessorWithIndex(declaredName, inThisClassOnly) }

    fun findMemberPropertyCustomAccessor(declaredName: String, inThisClassOnly: Boolean = false): PropertyAccessorsNode? =
        findMemberPropertyCustomAccessorWithIndex(declaredName, inThisClassOnly)?.first

    fun findMemberPropertyTransformedName(declaredName: String, inThisClassOnly: Boolean = false): String? =
        memberPropertyNameToTransformedName[declaredName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyTransformedName(declaredName, inThisClassOnly) }

    fun findMemberPropertyDeclaredName(transformedName: String, inThisClassOnly: Boolean = false): String? =
        memberTransformedNameToPropertyName[transformedName] ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberPropertyDeclaredName(transformedName, inThisClassOnly) }

    /**
     * Key: Function signature
     */
    fun getAllMemberFunctions(): Map<String, FunctionDeclarationNode> {
        return memberFunctionsMap.let { functionsInThisClass ->
            var result = functionsInThisClass
            if (superClass != null) {
                result = result mergeIfNotExists superClass!!.getAllMemberFunctions()
            }
            superInterfaces.forEach {
                result = result mergeIfNotExists it.getAllMemberFunctions()
            }
            result
        }
    }

    /**
     * Key: Function signature
     */
    fun getMemberFunctionsDeclaredInThisClass(): Map<String, FunctionDeclarationNode> {
        return memberFunctionsMap
    }

    @Deprecated("use findMemberFunctionsWithEnclosingTypeNameByDeclaredName")
    fun findMemberFunctionsWithIndexByDeclaredName(declaredName: String, inThisClassOnly: Boolean = false): Map<String, Pair<FunctionDeclarationNode, Int>> =
        memberFunctionsMap.filter { it.value.name == declaredName }.mapValues { it.value to index } mergeIfNotExists
            (Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberFunctionsWithIndexByDeclaredName(declaredName, inThisClassOnly) } ?: emptyMap() )

    fun findMemberFunctionsWithEnclosingTypeNameByDeclaredName(declaredName: String, inThisClassOnly: Boolean = false): Map<String, Pair<FunctionDeclarationNode, String>> =
        memberFunctionsMap.filter { it.value.name == declaredName }.mapValues { it.value to fullQualifiedName } mergeIfNotExists
            (Unit.takeIf { !inThisClassOnly }?.let {
                val result = mutableMapOf<String, Pair<FunctionDeclarationNode, String>>()
                superClass?.findMemberFunctionsWithEnclosingTypeNameByDeclaredName(declaredName, inThisClassOnly)
                    ?.also { result += it }
                superInterfaces.forEach { def ->
                    def.findMemberFunctionsWithEnclosingTypeNameByDeclaredName(declaredName, inThisClassOnly)?.also {
                        result += it
                    }
                }
                result
            } ?: emptyMap() )

    fun findMemberFunctionsByDeclaredName(declaredName: String, inThisClassOnly: Boolean = false): Map<String, FunctionDeclarationNode> =
        findMemberFunctionsWithEnclosingTypeNameByDeclaredName(declaredName, inThisClassOnly).mapValues { it.value.first }

    @Deprecated("use findMemberFunctionWithEnclosingTypeNameByTransformedName")
    fun findMemberFunctionWithIndexByTransformedName(transformedName: String, inThisClassOnly: Boolean = false): Pair<FunctionDeclarationNode, Int>? =
        memberFunctionsMap[transformedName]?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberFunctionWithIndexByTransformedName(transformedName, inThisClassOnly) }

    /**
     * For semantic analyzer use only. In SA, `memberFunctionsMap` is not indexed by transformedRefName.
     */
    fun findMemberFunctionWithIndexByTransformedNameLinearSearch(transformedName: String, inThisClassOnly: Boolean = false): Pair<FunctionDeclarationNode, Int>? =
        memberFunctionsMap.filter { it.value.transformedRefName == transformedName }.values.firstOrNull()?.let { it to index } ?:
            Unit.takeIf { !inThisClassOnly }?.let { superClass?.findMemberFunctionWithIndexByTransformedNameLinearSearch(transformedName, inThisClassOnly) }

    fun findMemberFunctionWithEnclosingTypeNameByTransformedName(transformedName: String, inThisClassOnly: Boolean = false): Pair<FunctionDeclarationNode, String>? =
        memberFunctionsMap[transformedName]?.let { it to fullQualifiedName } ?:
            Unit.takeIf { !inThisClassOnly }?.let {
                superClass?.findMemberFunctionWithEnclosingTypeNameByTransformedName(transformedName, inThisClassOnly)
                    ?: superInterfaces.firstNotNullOfOrNull { it.findMemberFunctionWithEnclosingTypeNameByTransformedName(transformedName, inThisClassOnly) }
            }

    /**
     * For semantic analyzer use only. In SA, `memberFunctionsMap` is not indexed by transformedRefName.
     */
    fun findMemberFunctionWithEnclosingTypeNameByTransformedNameLinearSearch(transformedName: String, inThisClassOnly: Boolean = false): Pair<FunctionDeclarationNode, String>? =
        memberFunctionsMap.filter { it.value.transformedRefName == transformedName }.values.firstOrNull()?.let { it to fullQualifiedName } ?:
            Unit.takeIf { !inThisClassOnly }?.let {
                superClass?.findMemberFunctionWithEnclosingTypeNameByTransformedNameLinearSearch(transformedName, inThisClassOnly)
                    ?: superInterfaces.firstNotNullOfOrNull { it.findMemberFunctionWithEnclosingTypeNameByTransformedNameLinearSearch(transformedName, inThisClassOnly) }
            }

    fun findMemberFunctionByTransformedName(transformedName: String, inThisClassOnly: Boolean = false): FunctionDeclarationNode? =
        findMemberFunctionWithEnclosingTypeNameByTransformedName(transformedName, inThisClassOnly)?.first

    fun findDeclarations(filter: (clazz: ClassDefinition, declaration: ASTNode) -> Boolean): List<ASTNode> {
        return declarations.filter { filter(this, it) } +
            (superClass?.findDeclarations(filter) ?: emptyList())
    }

    open fun construct(interpreter: Interpreter, callArguments: Array<RuntimeValue>, typeArguments: Array<DataType>, callPosition: SourcePosition): ClassInstance {
        return interpreter.constructClassInstance(callArguments, callPosition, typeArguments, this@ClassDefinition)
    }

    override fun toString(): String {
        return "ClassDefinition($fullQualifiedName)"
    }
}
