package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.error.DuplicateIdentifierException
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeToUpperBound
import com.sunnychung.lib.multiplatform.kotlite.log

open class SymbolTable(
    val scopeLevel: Int,
    val scopeName: String,
    val scopeType: ScopeType,
    val parentScope: SymbolTable?,
    val returnType: DataType? = null,
) {
    private val propertyDeclarations = mutableMapOf<String, PropertyType>()
    internal val propertyValues = mutableMapOf<String, RuntimeValueAccessor>()
    internal val propertyOwners = mutableMapOf<String, PropertyOwnerInfo>() // only use in SemanticAnalyzer
    internal val functionOwners = mutableMapOf<String, String>() // only use in SemanticAnalyzer
    internal val transformedSymbols = mutableMapOf<Pair<IdentifierClassifier, String>, String>() // only use in SemanticAnalyzer. transformed name -> original name

    protected val functionDeclarations = mutableMapOf<String, FunctionDeclarationNode>()
    protected val extensionFunctionDeclarations = mutableMapOf<String, FunctionDeclarationNode>()
    protected val extensionProperties = mutableMapOf<String, ExtensionProperty>()

    private val classDeclarations = mutableMapOf<String, ClassDefinition>()
    private val typeAlias = mutableMapOf<String, DataType>()
    private val typeAliasResolution = mutableMapOf<String, DataType>()

    fun declareProperty(name: String, type: TypeNode, isMutable: Boolean) {
        if (hasProperty(name = name, true)) {
            throw DuplicateIdentifierException(name = name, classifier = IdentifierClassifier.Property)
        }
        propertyDeclarations[name] = typeNodeToPropertyType(type = type, isMutable = isMutable)
            ?: throw RuntimeException("Unknown type ${type.name}")
    }

    fun findTypeAlias(name: String): Pair<DataType, SymbolTable>? {
        return typeAlias[name]?.let { it to this } ?: parentScope?.findTypeAlias(name)
    }

    fun declareTypeAlias(name: String, typeUpperBound: TypeNode?, referenceSymbolTable: SymbolTable = this) {
        if (typeAlias.containsKey(name) || typeAlias.containsKey("$name?")) {
            throw DuplicateIdentifierException(name, IdentifierClassifier.TypeAlias)
        }
        val typeUpperBound = typeUpperBound ?: TypeNode("Any", null, true)
        typeAlias[name] = referenceSymbolTable.typeNodeToDataType(typeUpperBound)!!
        typeAlias["$name?"] = referenceSymbolTable.typeNodeToDataType(typeUpperBound.copy(isNullable = true))!!
    }

    fun declareTypeAliasResolution(name: String, type: TypeNode, referenceSymbolTable: SymbolTable = this) {
        if (findTypeAlias(name) == null || findTypeAlias("$name?") == null) {
            throw RuntimeException("Type alias $name not found")
        }
        if (typeAliasResolution.containsKey(name) || typeAliasResolution.containsKey("$name?")) {
            throw DuplicateIdentifierException(name, IdentifierClassifier.TypeResolution)
        }
        typeAliasResolution[name] = referenceSymbolTable.typeNodeToDataType(type)!!
        typeAliasResolution["$name?"] = referenceSymbolTable.typeNodeToDataType(type.copy(isNullable = true))!!
    }

    fun declareTypeAliasResolution(name: String, type: DataType) {
        if (findTypeAlias(name) == null || findTypeAlias("$name?") == null) {
            throw RuntimeException("Type alias $name not found")
        }
        if (typeAliasResolution.containsKey(name) || typeAliasResolution.containsKey("$name?")) {
            throw DuplicateIdentifierException(name, IdentifierClassifier.TypeResolution)
        }
        typeAliasResolution[name] = type
        typeAliasResolution["$name?"] = type.copyOf(isNullable = true)
    }

    fun findTypeAliasResolution(name: String): DataType? {
        return typeAliasResolution[name] ?: parentScope?.findTypeAliasResolution(name)
    }

    fun assertToDataType(type: TypeNode): DataType {
        return typeNodeToDataType(type) ?: throw RuntimeException("Cannot resolve type ${type.descriptiveName()}")
    }

    fun typeNodeToDataType(type: TypeNode): DataType? {
        if (type.name == "*") {
            return AnyType(isNullable = true) // TODO any better handling? additional validations of use of type *?
        }

        val alias = findTypeAlias("${type.name}${if (type.isNullable) "?" else ""}")
        if (alias != null) {
            return findTypeAliasResolution("${type.name}${if (type.isNullable) "?" else ""}")
                ?: TypeParameterType(type.name, type.isNullable, alias.first)
        }
        if (type is FunctionTypeNode) {
            return FunctionType(
                arguments = type.parameterTypes?.map { assertToDataType(it) } ?: listOf(UnresolvedType),
                returnType = type.returnType?.let { assertToDataType(it) } ?: UnresolvedType,
                isNullable = type.isNullable,
            )
        }
        type.toPrimitiveDataType()?.let { return it }

        val clazz = findClass(type.name)?.first ?: return null
        // validate type arguments
        // TODO optimize so that we don't have to validate every time
        if (clazz.typeParameters.size != (type.arguments?.size ?: 0)) {
            throw RuntimeException("Number of type arguments (${type.arguments?.size ?: 0}) does not match with number of type parameters ${clazz.typeParameters.size} of class ${clazz.fullQualifiedName}")
        }
        type.arguments?.forEachIndexed { index, it ->
            // TODO refactor this repeated logic
            val upperBound = clazz.typeParameters[index].typeUpperBound ?: TypeNode("Any", null, true)
            if (!typeNodeToDataType(upperBound)!!.isAssignableFrom(typeNodeToDataType(it)!!)) {
                throw RuntimeException("Type argument ${it.descriptiveName()} is out of bound (${upperBound.descriptiveName()})")
            }
        }

        return ObjectType(
            clazz = clazz,
            arguments = type.arguments?.map { typeNodeToDataType(it)!! } ?: emptyList(),
            isNullable = type.isNullable
        )
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
            if (!type.type.isAssignableFrom(value.type())) {
                throw RuntimeException("Expected type ${type.type.nameWithNullable} but actual type is ${value.type().nameWithNullable}")
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

    fun declareFunction(name: String, node: FunctionDeclarationNode): String {
        val functionSignature = functionNameTransform(name = name, function = node)
        if (functionDeclarations.containsKey(functionSignature)) {
            throw DuplicateIdentifierException(name = name, classifier = IdentifierClassifier.Function)
        }
//        log.v(Exception()) { "Register $functionSignature at $scopeLevel" }
        functionDeclarations[functionSignature] = node
        return functionSignature
    }

    fun findFunction(name: String, isThisScopeOnly: Boolean = false): Pair<FunctionDeclarationNode, SymbolTable>? {
        return functionDeclarations[name]?.let { it  to this }
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.findFunction(name) }
    }

    fun declareExtensionFunction(name: String, node: FunctionDeclarationNode): String {
        val functionSignature = functionNameTransform(name = name, function = node)
        if (extensionFunctionDeclarations.containsKey(functionSignature)) {
            throw DuplicateIdentifierException(name = name, classifier = IdentifierClassifier.Function)
        }
        extensionFunctionDeclarations[functionSignature] = node
        return functionSignature
    }

    fun findExtensionFunction(transformedName: String, isThisScopeOnly: Boolean = false): FunctionDeclarationNode? {
        return extensionFunctionDeclarations[transformedName]
            ?: Unit.takeIf { !isThisScopeOnly }?.let { parentScope?.findExtensionFunction(transformedName) }
    }

    fun declareExtensionProperty(transformedName: String, extensionProperty: ExtensionProperty) {
        if (extensionProperties.containsKey(transformedName)) {
            throw DuplicateIdentifierException(name = transformedName, classifier = IdentifierClassifier.Property)
        }
        if (extensionProperty.receiverType == null) {
            extensionProperty.receiverType = extensionProperty.receiver.toTypeNode()
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
        if (receiverClass.memberPropertyNameToTransformedName.containsKey(extensionProperty.declaredName)) {
            throw DuplicateIdentifierException(name = extensionProperty.declaredName, classifier = IdentifierClassifier.Property)
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
            throw DuplicateIdentifierException(name = extensionProperty.declaredName, classifier = IdentifierClassifier.Property)
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

    fun declareClass(classDefinition: ClassDefinition) {
        if (findClass(classDefinition.fullQualifiedName) != null) {
            throw DuplicateIdentifierException(name = classDefinition.fullQualifiedName, classifier = IdentifierClassifier.Class)
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

    internal fun registerTransformedSymbol(identifierClassifier: IdentifierClassifier, transformedName: String, originalName: String) {
        val key = identifierClassifier to transformedName
        if (transformedSymbols.containsKey(key)) {
            throw DuplicateIdentifierException(transformedName, identifierClassifier)
        }
        transformedSymbols[key] = originalName
    }

    internal fun unregisterTransformedSymbol(identifierClassifier: IdentifierClassifier, transformedName: String): Boolean {
        val key = identifierClassifier to transformedName
        return if (transformedSymbols.containsKey(key)) {
            transformedSymbols.remove(key)
            true
        } else if (parentScope?.unregisterTransformedSymbol(identifierClassifier, transformedName) == true) {
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
            TypeParameterNode(it.key, it.value.toTypeNode())
        }
    }

    fun listTypeAliasInAllScopes(): List<TypeParameterNode> {
        return listTypeAliasInThisScope() + (parentScope?.listTypeAliasInAllScopes() ?: emptyList())
    }

    fun listTypeAliasResolutionInThisScope(): Map<String, DataType> {
        return typeAliasResolution
    }

    fun mergeFrom(other: SymbolTable) { // this is only involved in runtime
        log.d { "Merge from other SymbolTable" }
        other.propertyValues.forEach {
            putPropertyHolder(it.key, it.value)
        }
        other.functionDeclarations.forEach {
            declareFunction(it.key, it.value)
        }
        other.classDeclarations.forEach {
            declareClass(it.value)
        }
        other.typeAlias
            .filterKeys { !it.endsWith('?') }
            .forEach {
                // TODO handle conflicts with existing scope, e.g. generic functions
                declareTypeAlias(it.key, it.value.toTypeNode())
            }
        other.typeAliasResolution
            .filterKeys { !it.endsWith('?') }
            .forEach {
                // TODO handle conflicts with existing scope, e.g. generic functions
                declareTypeAliasResolution(it.key, it.value.toTypeNode())
            }
//        this.transformedSymbols += other.transformedSymbols
    }

    override fun toString(): String {
        return "scopeLevel = $scopeLevel\n" +
                "functionDeclarations = $functionDeclarations\n" +
                "propertyDeclarations = $propertyDeclarations\n" +
                "propertyValues = $propertyValues"
    }

}
