package com.sunnychung.lib.multiplatform.kotlite.model

/**
 * ExecutionEnvironment is stateful. Need to pass the same ExecutionEnvironment instance into both
 * SemanticAnalyzer and Interpreter.
 */
class ExecutionEnvironment(
//    private val registrationFilter: BuiltinFunctionRegistrationFilter = BuiltinFunctionRegistrationFilter { _ -> true }
    private val functionRegistrationFilter: (CustomFunctionDefinition) -> Boolean = { true },
    private val extensionPropertyRegistrationFilter: (ExtensionProperty) -> Boolean = { true },
    private val globalPropertyRegistrationFilter: (GlobalProperty) -> Boolean = { true },
    private val classRegistrationFilter: (String) -> Boolean = { true },
) {
    private val builtinFunctions: MutableList<CustomFunctionDeclarationNode> = mutableListOf()
    private val extensionProperties: MutableList<ExtensionProperty> = mutableListOf()
    private val globalProperties: MutableList<GlobalProperty> = mutableListOf()
    private val providedClasses: MutableList<ProvidedClassDefinition> = mutableListOf()
    private val initiallyProvidedClasses: MutableList<ProvidedClassDefinition> = mutableListOf()

    private val generatedMapping: MutableMap<MappingKey, AnalyzedMapping> = mutableMapOf()
    private val specialFunctionLookupCache: MutableMap<MappingKey, FunctionDeclarationNode> = mutableMapOf()

    init {
        registerInitClass(AnyClass.clazz)
        registerInitClass(ComparableInterface.interfaze)

        registerClass(PairClass.clazz)
        PairClass.properties.forEach {
            registerExtensionProperty(it)
        }

        registerInitClass(ThrowableValue.clazz)
        ThrowableValue.properties.forEach {
            registerExtensionProperty(it)
        }
        ThrowableValue.functions.forEach {
            registerFunction(it)
        }
        registerClass(ExceptionValue.clazz)
        registerClass(NullPointerExceptionValue.clazz)
        registerClass(TypeCastExceptionValue.clazz)

        registerClass(IteratorClass.clazz)
        IteratorClass.functions.forEach {
            registerFunction(it)
        }

        registerClass(IterableInterface.clazz)
        IterableInterface.functions.forEach {
            registerFunction(it)
        }
        
        registerClass(CollectionInterface.collectionClazz)
        registerClass(ListClass.clazz)
    }

    fun registerFunction(function: CustomFunctionDefinition) {
        if (functionRegistrationFilter(function)) {
            builtinFunctions += function.let {
                CustomFunctionDeclarationNode(it)
            }
        }
    }

    fun registerExtensionProperty(property: ExtensionProperty) {
        if (extensionPropertyRegistrationFilter(property)) {
            extensionProperties += property
        }
    }

    fun registerGlobalProperty(property: GlobalProperty) {
        if (globalPropertyRegistrationFilter(property)) {
            globalProperties += property
        }
    }

    fun registerClass(clazz: ProvidedClassDefinition) {
        if (classRegistrationFilter(clazz.fullQualifiedName)) {
            providedClasses += clazz
        }
    }

    fun registerInitClass(clazz: ProvidedClassDefinition) {
        if (classRegistrationFilter(clazz.fullQualifiedName)) {
            initiallyProvidedClasses += clazz
        }
    }

    internal fun getBuiltinFunctions(topmostSymbolTable: SymbolTable): List<CustomFunctionDeclarationNode> {
        return builtinFunctions.toList()
    }

    internal fun getExtensionProperties(topmostSymbolTable: SymbolTable): List<ExtensionProperty> {
        return extensionProperties.toList()
    }

    internal fun getGlobalProperties(topmostSymbolTable: SymbolTable): List<GlobalProperty> {
        return globalProperties.toList()
    }

    internal fun getBuiltinClasses(topmostSymbolTable: SymbolTable): List<ClassDefinition> {
        return initiallyProvidedClasses.filter { classRegistrationFilter(it.fullQualifiedName) }
            .flatMap {
                listOf(
                    it.copyClassDefinition(),
                    it.copyNullableClassDefinition(),
                    it.copyCompanionClassDefinition(),
                )
            } +
                listOf("Int", "Double", "Long", "Boolean", "String", "Char", "Byte", "Unit", "Nothing", "Function", "Class").flatMap { className ->
                    if (!classRegistrationFilter(className)) return@flatMap emptyList()
                    fun createTypeParameters(typeName: String): List<TypeParameterNode> {
                        return when (typeName) {
                            "Class" -> listOf(
                                TypeParameterNode(
                                    SourcePosition.BUILTIN,
                                    "T",
                                    TypeNode(SourcePosition.NONE, "Any", null, false)
                                )
                            )

                            else -> emptyList()
                        }
                    }

                    val interfaces = when (className) {
                        in setOf("Int", "Double", "Long", "Boolean", "String", "Char") -> {
                            listOf(
                                TypeNode(
                                    position = SourcePosition.BUILTIN,
                                    name = "Comparable",
                                    arguments = listOf(
                                        TypeNode(
                                            position = SourcePosition.BUILTIN,
                                            name = className,
                                            arguments = null,
                                            isNullable = false,
                                        )
                                    ),
                                    isNullable = false,
                                ) to ComparableInterface.memberFunctions.map {
                                    CustomFunctionDeclarationNode(
                                        it.copy(
                                            modifiers = setOf(
                                                // Intentionally drop "operator" modifier to lessen performance penalty.
                                                // Otherwise, it won't pass LoopTest.
                                                // FunctionModifier.operator,
                                                FunctionModifier.open,
                                                FunctionModifier.override,
                                            ),
                                            parameterTypes = listOf(
                                                CustomFunctionParameter(name = "other", type = className)
                                            ),
                                        )
                                    )
                                }
                            )
                        }

                        else -> emptyList()
                    }
                    listOf(
                        ClassDefinition(
                            currentScope = topmostSymbolTable,
                            name = className,
                            modifiers = emptySet(),
                            typeParameters = createTypeParameters(className),
                            isInstanceCreationAllowed = false,
                            orderedInitializersAndPropertyDeclarations = emptyList(),
                            declarations = emptyList(),
                            rawMemberProperties = emptyList(),
                            memberFunctions = interfaces.flatMap { it.second },
                            superInterfaceTypes = interfaces.map { it.first },
                            primaryConstructor = null,
                        ),
                        ClassDefinition(
                            currentScope = topmostSymbolTable,
                            name = "$className?",
                            modifiers = emptySet(),
                            typeParameters = createTypeParameters(className),
                            isInstanceCreationAllowed = false,
                            orderedInitializersAndPropertyDeclarations = emptyList(),
                            declarations = emptyList(),
                            rawMemberProperties = emptyList(),
                            memberFunctions = emptyList(),
                            primaryConstructor = null,
                        ),
                        ClassDefinition(
                            currentScope = topmostSymbolTable,
                            name = "$className.Companion",
                            modifiers = emptySet(),
                            typeParameters = createTypeParameters(className),
                            isInstanceCreationAllowed = false,
                            orderedInitializersAndPropertyDeclarations = emptyList(),
                            declarations = emptyList(),
                            rawMemberProperties = emptyList(),
                            memberFunctions = emptyList(),
                            primaryConstructor = null,
                        ),
                    )
                } +
                providedClasses.filter { classRegistrationFilter(it.fullQualifiedName) }
                    .flatMap {
                        listOf(
                            it.copyClassDefinition(),
                            it.copyNullableClassDefinition(),
                            it.copyCompanionClassDefinition(),
                        )
                    }
    }

    internal fun registerGeneratedMapping(type: SymbolType, receiverType: String?, parentName: String? = null, name: String, transformedName: String) {
        val key = MappingKey(type = type, receiverType = receiverType, parentName = parentName, name = name)
        generatedMapping[key] = AnalyzedMapping(key = key, transformedName = transformedName)
    }

    internal fun findGeneratedMapping(type: SymbolType, receiverType: String?, parentName: String? = null, name: String): AnalyzedMapping {
        val key = MappingKey(type = type, receiverType = receiverType, name = name, parentName = parentName)
        return generatedMapping[key]
            ?: throw RuntimeException("$type ${receiverType?.let { "$it." }}${parentName?.let { "$it." }}$name is not analyzed")
    }

    internal fun registerSpecialFunction(type: SymbolType, receiverType: String?, parentName: String? = null, name: String, function: FunctionDeclarationNode) {
        val key = MappingKey(type = type, receiverType = receiverType, parentName = parentName, name = name)
        specialFunctionLookupCache[key] = function
    }

    internal fun findNullableSpecialFunction(type: SymbolType, receiverType: String?, parentName: String? = null, name: String): FunctionDeclarationNode? {
        val key = MappingKey(type = type, receiverType = receiverType, name = name, parentName = parentName)
        return specialFunctionLookupCache[key]
    }

    internal fun findSpecialFunction(type: SymbolType, receiverType: String?, parentName: String? = null, name: String): FunctionDeclarationNode {
        return findNullableSpecialFunction(
            type = type,
            receiverType = receiverType,
            parentName = parentName,
            name = name
        )
            ?: throw RuntimeException("Function cache of $type ${receiverType?.let { "$it." }}${parentName?.let { "$it." }}$name is not found")
    }


    fun install(module: LibraryModule) {
        module.classes.forEach {
            registerClass(it)
        }
        module.properties.forEach {
            registerExtensionProperty(it)
        }
        module.globalProperties.forEach {
            registerGlobalProperty(it)
        }
        module.functions.forEach {
            registerFunction(it)
        }
    }

    internal data class MappingKey(val type: SymbolType, val receiverType: String?, val parentName: String? = null, val name: String)
    internal data class AnalyzedMapping(val key: MappingKey, val transformedName: String)

    internal enum class SymbolType {
        Function, ExtensionFunction, Property, ExtensionProperty, ValueParameter
    }
}
