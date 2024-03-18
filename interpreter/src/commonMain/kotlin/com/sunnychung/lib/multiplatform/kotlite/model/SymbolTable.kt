package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.error.DuplicateIdentifierException
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeArguments
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeToUpperBound
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.log
import com.sunnychung.lib.multiplatform.kotlite.util.ClassMemberResolver

open class SymbolTable(
    val scopeLevel: Int,
    val scopeName: String,
    val scopeType: ScopeType,
    var parentScope: SymbolTable?,
    val returnType: DataType? = null,
    val isInitOnCreate: Boolean = true,
) {
    init {
        if (parentScope == this) {
            throw RuntimeException("There is an immediate cycle in symbol table hierarchy")
        }

        val visitedParents = mutableSetOf(this)
        var parent = parentScope
        while (parent != null) {
            if (parent in visitedParents) {
                throw RuntimeException("There is a cycle in symbol table hierarchy")
            }
            parent = parent.parentScope
        }
    }

    private val propertyDeclarations = mutableMapOf<String, PropertyType>()
    internal val propertyValues = mutableMapOf<String, RuntimeValueAccessor>()
    internal val propertyOwners = mutableMapOf<String, PropertyOwnerInfo>() // only use in SemanticAnalyzer
    internal val functionOwners = mutableMapOf<String, String>() // only use in SemanticAnalyzer
    internal val transformedSymbols = mutableMapOf<Pair<IdentifierClassifier, String>, String>() // only use in SemanticAnalyzer. transformed name -> original name
    internal val transformedSymbolsByDeclaredName = mutableMapOf<Pair<IdentifierClassifier, String>, String>() // only use in SemanticAnalyzer. original name -> transformed name

    protected val functionDeclarations = mutableMapOf<String, FunctionDeclarationNode>()
    protected val extensionFunctionDeclarations = mutableMapOf<String, FunctionDeclarationNode>()
    protected val extensionProperties = mutableMapOf<String, ExtensionProperty>()

    private val classDeclarations = mutableMapOf<String, ClassDefinition>()
    private val typeAlias = mutableMapOf<String, DataType>()
    private val typeAliasResolution = mutableMapOf<String, DataType>()

    internal lateinit var rootScope: SymbolTable
    lateinit var IntType: PrimitiveType
        private set
    lateinit var LongType: PrimitiveType
        private set
    lateinit var DoubleType: PrimitiveType
        private set
    lateinit var BooleanType: PrimitiveType
        private set
    lateinit var StringType: PrimitiveType
        private set
    lateinit var CharType: PrimitiveType
        private set
    lateinit var ByteType: PrimitiveType
        private set
    lateinit var NullableIntType: PrimitiveType
        private set
    lateinit var NullableLongType: PrimitiveType
        private set
    lateinit var NullableDoubleType: PrimitiveType
        private set
    lateinit var NullableBooleanType: PrimitiveType
        private set
    lateinit var NullableStringType: PrimitiveType
        private set
    lateinit var NullableCharType: PrimitiveType
        private set
    lateinit var NullableByteType: PrimitiveType
        private set

    protected fun initPrimitiveTypes() {
        fun getPrimitiveClass(typeName: PrimitiveTypeName, isNullable: Boolean): ClassDefinition {
            return rootScope.findClass("${typeName.name}${if (isNullable) "?" else ""}", isThisScopeOnly = true)?.first
                ?: throw RuntimeException("Cannot find class ${typeName.name}")
        }

        fun getPrimitiveType(typeName: PrimitiveTypeName, isNullable: Boolean) : PrimitiveType {
            val nonNullableClass = getPrimitiveClass(typeName, isNullable = false)
            val nullableClass = getPrimitiveClass(typeName, isNullable = true)
            return PrimitiveType(
                typeName = typeName,
                isNullable = isNullable,
                nonNullableClass = nonNullableClass,
                nullableClass = nullableClass,
                superTypes = rootScope.resolveObjectType(
                    clazz = if (isNullable) nullableClass else nonNullableClass,
                    typeArguments = null,
                    isNullable = isNullable,
                ).superTypes
            )
        }

        if (scopeLevel == 0) {
            IntType = getPrimitiveType(PrimitiveTypeName.Int, isNullable = false)
            LongType = getPrimitiveType(PrimitiveTypeName.Long, isNullable = false)
            DoubleType = getPrimitiveType(PrimitiveTypeName.Double, isNullable = false)
            BooleanType = getPrimitiveType(PrimitiveTypeName.Boolean, isNullable = false)
            StringType = getPrimitiveType(PrimitiveTypeName.String, isNullable = false)
            CharType = getPrimitiveType(PrimitiveTypeName.Char, isNullable = false)
            ByteType = getPrimitiveType(PrimitiveTypeName.Byte, isNullable = false)
            NullableIntType = getPrimitiveType(PrimitiveTypeName.Int, isNullable = true)
            NullableLongType = getPrimitiveType(PrimitiveTypeName.Long, isNullable = true)
            NullableDoubleType = getPrimitiveType(PrimitiveTypeName.Double, isNullable = true)
            NullableBooleanType = getPrimitiveType(PrimitiveTypeName.Boolean, isNullable = true)
            NullableStringType = getPrimitiveType(PrimitiveTypeName.String, isNullable = true)
            NullableCharType = getPrimitiveType(PrimitiveTypeName.Char, isNullable = true)
            NullableByteType = getPrimitiveType(PrimitiveTypeName.Byte, isNullable = true)
        } else {
            IntType = rootScope.IntType
            LongType = rootScope.LongType
            DoubleType = rootScope.DoubleType
            BooleanType = rootScope.BooleanType
            StringType = rootScope.StringType
            CharType = rootScope.CharType
            ByteType = rootScope.ByteType
            NullableIntType = rootScope.NullableIntType
            NullableLongType = rootScope.NullableLongType
            NullableDoubleType = rootScope.NullableDoubleType
            NullableBooleanType = rootScope.NullableBooleanType
            NullableStringType = rootScope.NullableStringType
            NullableCharType = rootScope.NullableCharType
            NullableByteType = rootScope.NullableByteType
        }
    }

    init {
        if (parentScope != null || scopeLevel == 0) {
            rootScope = findScope(0)

            if (isInitOnCreate && scopeLevel > 1) { // user scopes
                init()
            }
        }
    }

    open fun init() {
        initPrimitiveTypes()
    }

    fun declareProperty(position: SourcePosition, name: String, type: TypeNode, isMutable: Boolean) {
        if (hasProperty(name = name, true)) {
            throw DuplicateIdentifierException(position = position, name = name, classifier = IdentifierClassifier.Property)
        }
        propertyDeclarations[name] = typeNodeToPropertyType(type = type, isMutable = isMutable)
            ?: throw RuntimeException("Unknown type ${type.name}")
    }

    open fun findTypeAlias(name: String): Pair<DataType, SymbolTable>? {
        return typeAlias[name]?.let { it to this } ?: parentScope?.findTypeAlias(name)
    }

    fun declareTypeAlias(position: SourcePosition, name: String, typeUpperBound: TypeNode?, referenceSymbolTable: SymbolTable = this) {
        if (typeAlias.containsKey(name) || typeAlias.containsKey("$name?")) {
            throw DuplicateIdentifierException(position, name, IdentifierClassifier.TypeAlias)
        }
        val typeUpperBound = typeUpperBound ?: TypeNode(SourcePosition.NONE, "Any", null, true)
        typeAlias[name] = SymbolTableTypeVisitCache(name).let { cache ->
            referenceSymbolTable.typeNodeToDataType(typeUpperBound, visitCache = cache)!!
                .also { result -> cache.postVisit(name, result) }
        }
        typeAlias["$name?"] = SymbolTableTypeVisitCache("$name?").let { cache ->
            referenceSymbolTable.typeNodeToDataType(typeUpperBound.copy(isNullable = true), visitCache = cache)!!
                .also { result -> cache.postVisit("$name?", result) }
        }
    }

    fun declareTypeAliasResolution(position: SourcePosition, name: String, type: TypeNode, referenceSymbolTable: SymbolTable = this) {
        if (findTypeAlias(name) == null || findTypeAlias("$name?") == null) {
            throw RuntimeException("Type alias $name not found")
        }
        if (typeAliasResolution.containsKey(name) || typeAliasResolution.containsKey("$name?")) {
            throw DuplicateIdentifierException(position, name, IdentifierClassifier.TypeResolution)
        }
        typeAliasResolution[name] = referenceSymbolTable.assertToDataType(type)
        typeAliasResolution["$name?"] = referenceSymbolTable.assertToDataType(type.copy(isNullable = true))
    }

    fun declareTypeAliasResolution(position: SourcePosition, name: String, type: DataType) {
        if (findTypeAlias(name) == null || findTypeAlias("$name?") == null) {
            throw RuntimeException("Type alias $name not found")
        }
        if (typeAliasResolution.containsKey(name) || typeAliasResolution.containsKey("$name?")) {
            throw DuplicateIdentifierException(position, name, IdentifierClassifier.TypeResolution)
        }
        typeAliasResolution[name] = type
        typeAliasResolution["$name?"] = type.copyOf(isNullable = true)
    }

    fun findTypeAliasResolution(name: String): DataType? {
        return typeAliasResolution[name] ?: parentScope?.findTypeAliasResolution(name)
    }

    fun assertToDataType(type: TypeNode, visitCache: SymbolTableTypeVisitCache = SymbolTableTypeVisitCache()): DataType {
        if (type.name == "<Repeated>") {
//            return assertToDataType(type.arguments!!.first(), visitCache)
            return RepeatedType(type.arguments!!.first().name, type.arguments!!.first().name.endsWith("?"))
        }

        val isCacheCreator = visitCache.isEmpty

        return typeNodeToDataType(type, visitCache)
            ?.also {
                if (isCacheCreator) {
                    visitCache.throwErrorIfThereIsUnprocessedRepeatedType()
                }
            }
            ?: throw RuntimeException("Cannot resolve type ${type.descriptiveName()}")
    }

    fun typeNodeToDataType(type: TypeNode, visitCache: SymbolTableTypeVisitCache = SymbolTableTypeVisitCache()): DataType? {
        if (type.name == "*") {
            return StarType // TODO: additional validations of use of type *?
        }

        if (visitCache.isVisited(type)) {
            return RepeatedType(type.descriptiveName(), type.isNullable).also {
                visitCache.postVisit(type, it)
            }
        }

        val alias = findTypeAlias("${type.name}${if (type.isNullable) "?" else ""}")
        if (alias != null) {
            return (findTypeAliasResolution("${type.name}${if (type.isNullable) "?" else ""}")
                ?: TypeParameterType(type.name, type.isNullable, alias.first)).also {
                visitCache.postVisit(type, it)
            }
        }
        if (type is FunctionTypeNode) {
            return FunctionType(
                arguments = type.parameterTypes?.map { assertToDataType(it) } ?: listOf(UnresolvedType),
                returnType = type.returnType?.let { assertToDataType(it) } ?: UnresolvedType,
                isNullable = type.isNullable,
            )
        }
        type.toPrimitiveDataType(rootScope)?.let { return it }

        val clazz = findClass(type.name)?.first ?: return null
        // validate type arguments
        // TODO optimize so that we don't have to validate every time
        if (clazz.typeParameters.size != (type.arguments?.size ?: 0)) {
            throw RuntimeException("Number of type arguments (${type.arguments?.size ?: 0}) does not match with number of type parameters ${clazz.typeParameters.size} of class ${clazz.fullQualifiedName}")
        }
        val typeArgumentMap = clazz.typeParameters.withIndex().associate { it.value.name to type.arguments!![it.index] }
        type.arguments?.forEachIndexed { index, it ->
            if (it.name == TypeNode.IGNORE.name) {
                return@forEachIndexed
            }

            // TODO refactor this repeated logic
            val upperBound = clazz.typeParameters[index].typeUpperBound?.resolveGenericParameterTypeArguments(typeArgumentMap) ?: TypeNode(SourcePosition.NONE, "Any", null, true)
            if (!assertToDataType(upperBound, visitCache.copy()).isAssignableFrom(assertToDataType(it, visitCache.copy()))) {
                throw RuntimeException("Type argument ${it.descriptiveName()} is out of bound (${upperBound.descriptiveName()})")
            }
        }

        val inputType = type
        val type = resolveObjectType(clazz, type.arguments, type.isNullable, visitCache = visitCache)
        if (type!!.clazz != clazz) {
            throw RuntimeException("genericResolver.genericResolutions is wrong")
        }

        return type.also { visitCache.postVisit(inputType, it) }
//        return ObjectType(
//            clazz = clazz,
//            arguments = type.arguments?.map { assertToDataType(it) } ?: emptyList(),
//            isNullable = type.isNullable
//        )
    }

    fun resolveObjectType(clazz: ClassDefinition, typeArguments: List<TypeNode>?, isNullable: Boolean, upToIndex: Int = -1, visitCache: SymbolTableTypeVisitCache = SymbolTableTypeVisitCache()): ObjectType {
        val genericResolver = ClassMemberResolver.create(this, clazz, typeArguments ?: emptyList())!!
//        var superType: ObjectType? = null
//        genericResolver.genericResolutions.forEachIndexed { index, resolutions ->
//            if (upToIndex >= 0 && index > upToIndex) return superType
//            val clazz = resolutions.first
//            superType = ObjectType(
//                clazz = clazz,
//                arguments = clazz.typeParameters.map { tp ->
//                    val argument = resolutions.second[tp.name]!!
//                    assertToDataType(argument)
//                },
//                isNullable = isNullable,
//                superType = superType
//            )
//        }
//        return superType

//        val visitedTypes = mutableSetOf<String>()

        fun resolve(type: ClassDefinition): ObjectType {
            val resolution = genericResolver.genericResolutionsByTypeName[type.name]!!
            visitCache.preVisit(type)
//            log.v { "resolve visit ${type.name}" }
//            log.v { "resolve visited = ${visitedTypes}" }
            return ObjectType(
                clazz = type,
                arguments = type.typeParameters.map { tp ->
                    val argument = resolution[tp.name]!!
                    assertToDataType(argument, visitCache)
                },
                isNullable = isNullable,
                superTypes = (listOfNotNull(type.superClass) + type.superInterfaces)
                    .flatMap {
                        if (visitCache.isVisited(it)) {
                            return@flatMap emptyList()
                        }
                        log.v { "resolve ${it.name} from ${type.name}" }
                        val superType = resolve(it)
                        log.v { "superType ${superType.descriptiveName} with super types ${superType.superTypes}" }
                        listOf(superType) + superType.superTypes
                     }
                    .groupBy { it.name }
                    .mapValues {
                        it.value.indices.forEach { i ->
                            if (i > 0) {
                                if (it.value[i].arguments != it.value[i - 1].arguments) {
                                    throw RuntimeException("Type arguments of repeated type ${it.key} are not consistent -- ${it.value[i].arguments} VS ${it.value[i - 1].arguments}")
                                }
                            }
                        }
                        it.value.first()
                    }
                    .values.toList(),
            ).also {
                visitCache.postVisit(type, it)
            }
        }

        val isCacheCreator = visitCache.isEmpty

        return resolve(clazz).also {
            if (isCacheCreator) {
                visitCache.throwErrorIfThereIsUnprocessedRepeatedType()
            }
        }
    }

    fun typeNodeToPropertyType(type: TypeNode, isMutable: Boolean): PropertyType? {
        val dataType = typeNodeToDataType(type) ?: return null
        return PropertyType(type = dataType, isMutable = isMutable)
    }

    fun undeclareProperty(name: String) {
        if (!hasProperty(name = name, true)) {
            throw RuntimeException("No such property `$name`")
        }
        propertyDeclarations.remove(name)
        propertyValues.remove(name)
    }

    fun undeclarePropertyByDeclaredName(declaredName: String) {
        undeclareProperty(findTransformedNameByDeclaredName(declaredName))
    }

    /**
     * Only use in SemanticAnalyzer
     */
    fun declarePropertyOwner(name: String, owner: String, extensionPropertyRef: String? = null) {
        propertyOwners[name] = PropertyOwnerInfo(ownerRefName = owner, extensionPropertyRef = extensionPropertyRef)
    }

    /**
     * Only use in SemanticAnalyzer
     */
    fun declareFunctionOwner(name: String, function: FunctionDeclarationNode, owner: String) {
        functionOwners[functionNameTransform(name, function)] = owner
    }

    /**
     * Only use in SemanticAnalyzer
     *
     * @param name use transformed name
     */
    fun findPropertyOwner(name: String): PropertyOwnerInfo? {
        if (propertyOwners.containsKey(name)) {
            return propertyOwners[name]
        } else {
            return parentScope?.findPropertyOwner(name)
        }
    }

    /**
     * Only use in SemanticAnalyzer
     */
    fun findFunctionOwner(name: String): String? {
        if (functionOwners.containsKey(name)) {
            return functionOwners[name]
        } else {
            return parentScope?.findFunctionOwner(name)
        }
    }

    fun assign(name: String, value: RuntimeValue): Boolean {
        if (propertyDeclarations.containsKey(name)) {
            val type = propertyDeclarations[name]!!
//            if (!type.isMutable && propertyValues.containsKey(name)) {
//                throw RuntimeException("val cannot be reassigned")
//            }
            if (!type.type.isConvertibleFrom(value.type()) && type.type != value.type()) {
                throw RuntimeException("Expected type ${type.type.descriptiveName} but actual type is ${value.type().descriptiveName}")
            }
            propertyValues.getOrPut(name) { RuntimeValueHolder(type.type, type.isMutable, null) }.assign(value = value)
            return true
        } else if (parentScope?.assign(name, value) == true) {
            return true
        } else {
            throw RuntimeException("The variable `$name` has not been declared")
        }
    }

    fun hasAssignedInThisScope(name: String): Boolean {
        return propertyValues[name] != null
    }

    fun getPropertyTypeOrNull(name: String, isThisScopeOnly: Boolean = false): Pair<PropertyType, SymbolTable>? {
        return (propertyDeclarations[name]?.let { it to this }
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.getPropertyTypeOrNull(name) })
            ?.let { result ->
                if (result.first.type is TypeParameterType) {
                    findTypeAliasResolution(result.first.type.name)?.let {
                        return@let PropertyType(it, result.first.isMutable) to result.second
                    }
                }
                result
            }
    }

    fun getPropertyType(name: String, isThisScopeOnly: Boolean = false): Pair<PropertyType, SymbolTable> {
        return getPropertyTypeOrNull(name = name, isThisScopeOnly = isThisScopeOnly)
            ?: throw RuntimeException("The variable `$name` has not been declared")
    }

    fun read(name: String, isThisScopeOnly: Boolean = false): RuntimeValue {
        return propertyValues[name]?.read()
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.read(name) }
            ?: throw RuntimeException("The variable `$name` has not been declared")
    }

    fun putPropertyHolder(name: String, holder: RuntimeValueAccessor) {
        if (propertyValues.containsKey(name)) {
            throw RuntimeException("Property `$name` has already been defined")
        }
        propertyDeclarations[name] = PropertyType(holder.type, false)
        propertyValues[name] = holder
    }

    fun getPropertyHolder(name: String, isThisScopeOnly: Boolean = false): RuntimeValueAccessor {
        return propertyValues[name]
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.getPropertyHolder(name) }
            ?: throw RuntimeException("The variable `$name` has not been declared")
    }

    fun hasProperty(name: String, isThisScopeOnly: Boolean = false): Boolean {
        val thisScopeResult = propertyDeclarations.containsKey(name)
        if (isThisScopeOnly) {
            return thisScopeResult
        }
        return thisScopeResult || (parentScope?.hasProperty(name) ?: false)
    }

    fun declareFunction(position: SourcePosition, name: String, node: FunctionDeclarationNode): String {
        val functionSignature = functionNameTransform(name = name, function = node)
        if (functionDeclarations.containsKey(functionSignature)) {
            throw DuplicateIdentifierException(position = position, name = name, classifier = IdentifierClassifier.Function)
        }
//        log.v(Exception()) { "Register $functionSignature at $scopeLevel" }
        functionDeclarations[functionSignature] = node
        return functionSignature
    }

    fun findFunction(name: String, isThisScopeOnly: Boolean = false): Pair<FunctionDeclarationNode, SymbolTable>? {
        return functionDeclarations[name]?.let { it  to this }
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.findFunction(name) }
    }

    fun declareExtensionFunction(position: SourcePosition, name: String, node: FunctionDeclarationNode): String {
        val functionSignature = functionNameTransform(name = name, function = node)
        if (extensionFunctionDeclarations.containsKey(functionSignature)) {
            throw DuplicateIdentifierException(position = position, name = name, classifier = IdentifierClassifier.Function)
        }
        extensionFunctionDeclarations[functionSignature] = node
        return functionSignature
    }

    fun findExtensionFunction(transformedName: String, isThisScopeOnly: Boolean = false): FunctionDeclarationNode? {
        return extensionFunctionDeclarations[transformedName]
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.findExtensionFunction(transformedName) }
    }

    fun findExtensionFunctionsByDeclaredName(receiver: TypeNode, declaredName: String, isThisScopeOnly: Boolean = false): Collection<FunctionDeclarationNode> {
        return extensionFunctionDeclarations.filter { it.value.receiver?.name == receiver.name && it.value.name == declaredName }.values + // TODO handle generics
            (Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.findExtensionFunctionsByDeclaredName(receiver, declaredName, isThisScopeOnly) } ?: emptyList())
    }

    fun declareExtensionProperty(position: SourcePosition, transformedName: String, extensionProperty: ExtensionProperty) {
        if (extensionProperties.containsKey(transformedName)) {
            throw DuplicateIdentifierException(position = position, name = transformedName, classifier = IdentifierClassifier.Property)
        }
        if (extensionProperty.typeNode == null) {
            Parser(Lexer(position.filename, extensionProperty.type)).type().also { type ->
                extensionProperty.typeNode = type
            }
        }
        if (extensionProperty.receiverType == null) {
            extensionProperty.receiverType = extensionProperty.receiver.toTypeNode("")
        }
        val receiverType = extensionProperty.receiverType!!
        val receiverClass = findClass(receiverType.name)?.first ?: throw RuntimeException("Class `${receiverType.name}` not found")
        if (receiverClass.typeParameters.size != (receiverType.arguments?.size ?: 0)) {
            throw RuntimeException("Number of type parameters of class `${receiverType.name}` mismatch")
        }
        val extensionTypeParameters = extensionProperty.typeParameters.toTypeParameterNodes()
        receiverClass.typeParameters.forEachIndexed { index, tp ->
            if (!assertToDataType(tp.typeUpperBoundOrAny()).isAssignableFrom(
                    assertToDataType(
                        receiverType.arguments!![index]
                            .resolveGenericParameterTypeToUpperBound(extensionTypeParameters)
                    )
            )) {
                throw RuntimeException("Provided type parameter `${tp.name}` of the class `${receiverType.name}` is out of bound (Upper bound: `${tp.typeUpperBoundOrAny().descriptiveName()}`)")
            }
        }
        if (receiverClass.findMemberPropertyTransformedName(extensionProperty.declaredName) != null) {
            throw DuplicateIdentifierException(position = position, name = extensionProperty.declaredName, classifier = IdentifierClassifier.Property)
        }
        val resolvedReceiverType = extensionProperty.receiverType!!.resolveGenericParameterTypeToUpperBound(extensionTypeParameters)
        if (extensionProperties.any {
            val existingResolvedReceiverType = it.value.receiverType!!.resolveGenericParameterTypeToUpperBound(it.value.typeParameters.toTypeParameterNodes())
            existingResolvedReceiverType.name == resolvedReceiverType.name &&
            existingResolvedReceiverType.arguments?.withIndex()?.all {
                it.value.name == resolvedReceiverType.arguments!![it.index].name &&
                it.value.isNullable == resolvedReceiverType.arguments!![it.index].isNullable
            } != false &&
            it.value.declaredName == extensionProperty.declaredName
        }) {
            throw DuplicateIdentifierException(position = position, name = extensionProperty.declaredName, classifier = IdentifierClassifier.Property)
        }
        extensionProperties[transformedName] = extensionProperty
    }

    fun findExtensionProperty(name: String, isThisScopeOnly: Boolean = false): ExtensionProperty? {
        return extensionProperties[name]
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.findExtensionProperty(name) }
    }

    /**
     * @param resolvedReceiver resolved means there is no generic type parameter
     */
    fun findExtensionPropertyByDeclaration(resolvedReceiver: TypeNode, declaredName: String, isThisScopeOnly: Boolean = false): Pair<String, ExtensionProperty>? {
        return extensionProperties.asSequence()
            .filter {
                it.value.receiverType!!.name == resolvedReceiver.name &&
                        (resolvedReceiver.isNullable || !it.value.receiverType!!.isNullable) &&
                        it.value.declaredName == declaredName
            }
            // Here assumes at most 3 same-name extension properties declared: exact type or Any? or Any. exact type one has higher precedence
            .let {
                it.firstOrNull { it.value.receiverType!!.descriptiveName() == resolvedReceiver.descriptiveName() }
                    ?: it.firstOrNull()
            }
            ?.toPair()
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.findExtensionPropertyByDeclaration(resolvedReceiver, declaredName, isThisScopeOnly) }
    }

    /**
     * @param resolvedReceiver resolved means there is no generic type parameter
     */
    fun findExtensionPropertyByReceiver(resolvedReceiver: TypeNode, isThisScopeOnly: Boolean = false): List<Pair<String, ExtensionProperty>> {
        return extensionProperties
            .asSequence()
            .filter { it.value.receiverType!!.name == resolvedReceiver.name && (resolvedReceiver.isNullable || !it.value.receiverType!!.isNullable) }
            .map { it.toPair() }
            .groupBy { it.second.declaredName }
            // Here assumes at most 3 same-name extension properties declared: exact type or Any? or Any. exact type one has higher precedence
            .mapValues { it.value.firstOrNull { it.second.receiverType!!.descriptiveName() == resolvedReceiver.descriptiveName() } ?: it.value.first() }
            .map { it.value }
            .toList() +
            (Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.findExtensionPropertyByReceiver(resolvedReceiver, isThisScopeOnly) }
                ?: emptyList())
    }

    fun findTransformedNameByDeclaredName(declaredName: String): String
        = propertyValues.keys.firstOrNull { it.substring(0 ..< it.lastIndexOf('/')) == declaredName }!!

    fun findPropertyByDeclaredName(declaredName: String): RuntimeValue? {
        return findTransformedNameByDeclaredName(declaredName)
            .let { transformedName -> propertyValues[transformedName]?.read() }
    }

    fun declareClass(position: SourcePosition, classDefinition: ClassDefinition) {
        if (findClass(classDefinition.fullQualifiedName) != null) {
            throw DuplicateIdentifierException(position = position, name = classDefinition.fullQualifiedName, classifier = IdentifierClassifier.Class)
        }
        classDeclarations[classDefinition.fullQualifiedName] = classDefinition
    }

    fun findClass(fullQualifiedName: String, isThisScopeOnly: Boolean = false): Pair<ClassDefinition, SymbolTable>? {
        return if (classDeclarations.containsKey(fullQualifiedName)) {
            classDeclarations[fullQualifiedName]!! to this
        } else if (!isThisScopeOnly) {
            parentScope?.findClass(fullQualifiedName)
        } else {
            null
        }
    }

    open fun functionNameTransform(name: String, function: FunctionDeclarationNode): String {
        return name
    }

    // this is expensive
    fun findFunctionOrExtensionFunctionIncludingSuperclassesByDeclaredName(receiver: TypeNode, declaredName: String): Collection<FunctionDeclarationNode> {
        val clazz = findClass(receiver.name)!!.first
        clazz.findMemberFunctionsByDeclaredName(declaredName).values
            .also {
                if (it.isNotEmpty()) {
                    return it
                }
            }

        var type: DataType = assertToDataType(receiver)
//        while (type != null) {
        (listOf(type) + ((type as? ObjectType)?.superTypes ?: emptyList())).forEach { type ->
            findExtensionFunctionsByDeclaredName(type.toTypeNode(), declaredName).also {
                if (it.isNotEmpty()) {
                    return it
                }
            }
//            type = (type as? ObjectType)?.superType
        }

        throw RuntimeException("Function $declaredName for receiver ${receiver.descriptiveName()} not found")
    }

    private fun isReverseTransformNeeded(identifierClassifier: IdentifierClassifier): Boolean =
        when (identifierClassifier) {
            IdentifierClassifier.Property, IdentifierClassifier.Class, IdentifierClassifier.TypeAlias -> true
            IdentifierClassifier.Function, IdentifierClassifier.TypeResolution -> false
        }

    internal fun registerTransformedSymbol(position: SourcePosition, identifierClassifier: IdentifierClassifier, transformedName: String, originalName: String) {
        val key = identifierClassifier to transformedName
        if (transformedSymbols.containsKey(key) || (isReverseTransformNeeded(identifierClassifier) && transformedSymbolsByDeclaredName.containsKey(identifierClassifier to originalName))) {
            throw DuplicateIdentifierException(position, transformedName, identifierClassifier)
        }
        transformedSymbols[key] = originalName
        if (isReverseTransformNeeded(identifierClassifier)) {
            transformedSymbolsByDeclaredName[identifierClassifier to originalName] = transformedName
        }
    }

    internal fun unregisterTransformedSymbol(identifierClassifier: IdentifierClassifier, transformedName: String, originalName: String): Boolean {
        val key = identifierClassifier to transformedName
        return if (transformedSymbols.containsKey(key)) {
            transformedSymbols.remove(key)
            if (isReverseTransformNeeded(identifierClassifier)) {
                transformedSymbolsByDeclaredName.remove(identifierClassifier to originalName).also {
                    if (it == null) {
                        throw RuntimeException("$identifierClassifier orig $originalName not found")
                    }
                }
            }
            true
        } else if (parentScope?.unregisterTransformedSymbol(identifierClassifier, transformedName, originalName) == true) {
            true
        } else {
            throw RuntimeException("$identifierClassifier $transformedName not found")
        }
    }

    fun findTransformedSymbol(identifierClassifier: IdentifierClassifier, transformedName: String): Pair<String, SymbolTable>? {
        val key = identifierClassifier to transformedName
        return transformedSymbols[key]?.let { it to this }
            ?: parentScope?.findTransformedSymbol(identifierClassifier, transformedName) // TODO optimize to pass key directly
    }

    fun listTypeAliasInThisScope(): List<TypeParameterNode> {
        return typeAlias.map {
            TypeParameterNode(SourcePosition.NONE, it.key, it.value.toTypeNode())
        }
    }

    fun listTypeAliasInAllScopes(): List<TypeParameterNode> {
        return listTypeAliasInThisScope() + (parentScope?.listTypeAliasInAllScopes() ?: emptyList())
    }

    fun listTypeAliasResolutionInThisScope(): Map<String, DataType> {
        return typeAliasResolution
    }

    fun findScope(level: Int): SymbolTable {
        if (level > scopeLevel) {
            throw RuntimeException("Cannot find scope with level $level")
        }
        var scope: SymbolTable? = this
        while (scope != null && scope.scopeLevel != level) {
            scope = scope.parentScope
        }
        if (scope != null) {
            return scope
        } else {
            throw RuntimeException("Cannot find scope with level $level")
        }
    }

    fun mergeFrom(position: SourcePosition, other: SymbolTable) { // this is only involved in runtime
        log.d { "Merge from other SymbolTable" }
        other.propertyValues.forEach {
            putPropertyHolder(it.key, it.value)
        }
        other.functionDeclarations.forEach {
            declareFunction(position, it.key, it.value)
        }
        other.classDeclarations.forEach {
            declareClass(position, it.value)
        }
        other.typeAlias
            .filterKeys { !it.endsWith('?') }
            .forEach {
                // TODO handle conflicts with existing scope, e.g. generic functions
                declareTypeAlias(position, it.key, it.value.toTypeNode())
            }
        other.typeAliasResolution
            .filterKeys { !it.endsWith('?') }
            .forEach {
                // TODO handle conflicts with existing scope, e.g. generic functions
                declareTypeAliasResolution(position, it.key, it.value.toTypeNode())
            }
//        this.transformedSymbols += other.transformedSymbols
    }

    fun mergeDeclarationsFrom(position: SourcePosition, other: SymbolTable, typeAliasResolution: Map<String, TypeNode>) {
        log.d { "Merge declarations from other SymbolTable" }
        other.typeAlias
            .filterKeys { !it.endsWith('?') }
            .forEach {
                // TODO handle conflicts with existing scope, e.g. generic functions
                declareTypeAlias(position, it.key, it.value.toTypeNode())
                typeAliasResolution[it.key]?.let { resolution ->
                    declareTypeAliasResolution(position, it.key, resolution)
                }
            }
        other.propertyDeclarations.forEach {
            declareProperty(position, it.key, it.value.type.toTypeNode(), it.value.isMutable)
        }
        other.extensionFunctionDeclarations.forEach {
            declareExtensionFunction(position, it.key, it.value)
        }
        other.extensionProperties.forEach {
            declareExtensionProperty(position, it.key, it.value)
        }
        other.functionDeclarations.forEach {
            declareFunction(position, it.key, it.value)
        }
        other.classDeclarations.forEach {
            declareClass(position, it.value)
        }
        other.propertyOwners.forEach {
            declarePropertyOwner(it.key, it.value.ownerRefName, it.value.extensionPropertyRef)
        }
        other.transformedSymbols.forEach {
            registerTransformedSymbol(SourcePosition.NONE, it.key.first, it.key.second, it.value)
        }
    }

    fun printSymbolTableStack() {
        log.d {
            buildString {
                var table: SymbolTable? = this@SymbolTable
                while (table != null) {
                    if (isNotEmpty()) {
                        append(" --> ")
                    }
                    append("[l=${table.scopeLevel}, name=${table.scopeName}, type=${table.scopeType}]")
                    table = table.parentScope
                }
            }
        }
    }

    override fun toString(): String {
        return "scopeLevel = $scopeLevel\n" +
                "functionDeclarations = $functionDeclarations\n" +
                "propertyDeclarations = $propertyDeclarations\n" +
                "propertyValues = $propertyValues"
    }

}
