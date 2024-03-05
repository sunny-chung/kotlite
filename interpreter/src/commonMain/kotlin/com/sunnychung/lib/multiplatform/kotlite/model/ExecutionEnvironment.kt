package com.sunnychung.lib.multiplatform.kotlite.model

/**
 * ExecutionEnvironment is stateful. Need to pass the same ExecutionEnvironment instance into both
 * SemanticAnalyzer and Interpreter.
 */
class ExecutionEnvironment(
//    private val registrationFilter: BuiltinFunctionRegistrationFilter = BuiltinFunctionRegistrationFilter { _ -> true }
    private val functionRegistrationFilter: (CustomFunctionDefinition) -> Boolean = { true },
    private val propertyRegistrationFilter: (ExtensionProperty) -> Boolean = { true },
    private val classRegistrationFilter: (String) -> Boolean = { true },
) {
    private val builtinFunctions: MutableList<CustomFunctionDeclarationNode> = mutableListOf()
    private val extensionProperties: MutableList<ExtensionProperty> = mutableListOf()
    private val providedClasses: MutableList<ProvidedClassDefinition> = mutableListOf()

    private val generatedMapping: MutableMap<MappingKey, AnalyzedMapping> = mutableMapOf()

    init {
        registerClass(PairValue.clazz)
        PairValue.properties.forEach {
            registerExtensionProperty(it)
        }

        registerClass(ThrowableValue.clazz)
        ThrowableValue.properties.forEach {
            registerExtensionProperty(it)
        }
        ThrowableValue.functions.forEach {
            registerFunction(it)
        }
        registerClass(ExceptionValue.clazz)
        registerClass(NullPointerExceptionValue.clazz)
        registerClass(TypeCastExceptionValue.clazz)

        registerClass(IterableInterface.clazz)
        registerClass(CollectionInterface.collectionClazz)
        registerClass(ListValue.clazz)
    }

    fun registerFunction(function: CustomFunctionDefinition) {
        if (functionRegistrationFilter(function)) {
            builtinFunctions += function.let {
                CustomFunctionDeclarationNode(it)
            }
        }
    }

    fun registerExtensionProperty(property: ExtensionProperty) {
        if (propertyRegistrationFilter(property)) {
            extensionProperties += property
        }
    }

    fun registerClass(clazz: ProvidedClassDefinition) {
        if (classRegistrationFilter(clazz.fullQualifiedName)) {
            providedClasses += clazz
        }
    }

    internal fun getBuiltinFunctions(topmostSymbolTable: SymbolTable): List<CustomFunctionDeclarationNode> {
        return builtinFunctions.toList()
    }

    internal fun getExtensionProperties(topmostSymbolTable: SymbolTable): List<ExtensionProperty> {
        return extensionProperties.toList()
    }

    internal fun getBuiltinClasses(topmostSymbolTable: SymbolTable): List<ClassDefinition> {
        return listOf("Int", "Double", "Long", "Boolean", "String", "Char", "Unit", "Nothing", "Function", "Class", "Any").flatMap { className ->
            if (!classRegistrationFilter(className)) return@flatMap emptyList()
            fun createTypeParameters(typeName: String): List<TypeParameterNode> {
                return when (typeName) {
                    "Class" -> listOf(TypeParameterNode(SourcePosition.BUILTIN, "T", TypeNode(SourcePosition.NONE, "Any", null, false)))
                    else -> emptyList()
                }
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
                    memberFunctions = emptyMap(),
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
                    memberFunctions = emptyMap(),
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
                    memberFunctions = emptyMap(),
                    primaryConstructor = null,
                ),
            )
        } +
                providedClasses.filter { classRegistrationFilter(it.fullQualifiedName) }
                    .flatMap {
                        listOf(
                            it,
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


    fun install(module: LibraryModule) {
        module.classes.forEach {
            registerClass(it)
        }
        module.properties.forEach {
            registerExtensionProperty(it)
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
