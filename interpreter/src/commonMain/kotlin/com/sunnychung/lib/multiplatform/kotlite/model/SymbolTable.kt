package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.error.DuplicateIdentifierException
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier

class SymbolTable(
    val scopeLevel: Int,
    val scopeName: String,
    val scopeType: ScopeType,
    val parentScope: SymbolTable?,
    val returnType: DataType? = null,
) {
    private val propertyDeclarations = mutableMapOf<String, PropertyType>()
    internal val propertyValues = mutableMapOf<String, RuntimeValueAccessor>()
    internal val propertyOwners = mutableMapOf<String, String>() // only use in SemanticAnalyzer
    internal val functionOwners = mutableMapOf<String, String>() // only use in SemanticAnalyzer

    private val functionDeclarations = mutableMapOf<String, FunctionDeclarationNode>()
    private val extensionFunctionDeclarations = mutableMapOf<String, FunctionDeclarationNode>()

    private val classDeclarations = mutableMapOf<String, ClassDefinition>()

    fun declareProperty(name: String, type: TypeNode, isMutable: Boolean) {
        if (hasProperty(name = name, true)) {
            throw DuplicateIdentifierException(name = name, classifier = IdentifierClassifier.Property)
        }
        propertyDeclarations[name] = typeNodeToPropertyType(type = type, isMutable = isMutable)
            ?: throw RuntimeException("Unknown type ${type.name}")
    }

    fun typeNodeToDataType(type: TypeNode): DataType? {
        if (type is FunctionTypeNode) {
            return FunctionType(
                arguments = type.parameterTypes.map { typeNodeToDataType(it)!! },
                returnType = typeNodeToDataType(type.returnType)!!,
                isNullable = type.isNullable,
            )
        }
        val primitiveType = type.toPrimitiveDataType()
        return primitiveType ?: ObjectType(clazz = findClass(type.name)?.first ?: return null, isNullable = type.isNullable)
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
    fun declarePropertyOwner(name: String, owner: String) {
        propertyOwners[name] = owner
    }

    /**
     * Only use in SemanticAnalyzer
     */
    fun declareFunctionOwner(name: String, owner: String) {
        functionOwners[name] = owner
    }

    /**
     * Only use in SemanticAnalyzer
     *
     * @param name use transformed name
     */
    fun findPropertyOwner(name: String): String? {
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
                throw RuntimeException("Type ${value.type().name} cannot be casted to ${type.type.name}")
            }
            propertyValues.getOrPut(name) { RuntimeValueHolder(type.type, type.isMutable, null) }.assign(value)
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
        return propertyDeclarations[name]?.let { it to this }
            ?: Unit.takeIf { !isThisScopeOnly }.let { parentScope?.getPropertyTypeOrNull(name) }
    }

    fun getPropertyType(name: String, isThisScopeOnly: Boolean = false): Pair<PropertyType, SymbolTable> {
        return getPropertyTypeOrNull(name = name, isThisScopeOnly = isThisScopeOnly)
            ?: throw RuntimeException("The variable `$name` has not been declared")
    }

    fun read(name: String, isThisScopeOnly: Boolean = false): RuntimeValue {
        return propertyValues[name]?.read()
            ?: Unit.takeIf { !isThisScopeOnly }.let { parentScope?.read(name) }
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
            ?: Unit.takeIf { !isThisScopeOnly }.let { parentScope?.getPropertyHolder(name) }
            ?: throw RuntimeException("The variable `$name` has not been declared")
    }

    fun hasProperty(name: String, isThisScopeOnly: Boolean = false): Boolean {
        val thisScopeResult = propertyDeclarations.containsKey(name)
        if (isThisScopeOnly) {
            return thisScopeResult
        }
        return thisScopeResult || (parentScope?.hasProperty(name) ?: false)
    }

    fun declareFunction(name: String, node: FunctionDeclarationNode) {
        val functionSignature = "$name"
        if (functionDeclarations.containsKey(functionSignature)) {
            throw DuplicateIdentifierException(name = name, classifier = IdentifierClassifier.Function)
        }
        functionDeclarations[functionSignature] = node
    }

    fun findFunction(name: String): Pair<FunctionDeclarationNode, SymbolTable>? {
        return functionDeclarations[name]?.let { it  to this } ?: parentScope?.findFunction(name)
    }

    fun declareExtensionFunction(name: String, node: FunctionDeclarationNode) {
        val functionSignature = "$name"
        if (extensionFunctionDeclarations.containsKey(functionSignature)) {
            throw DuplicateIdentifierException(name = name, classifier = IdentifierClassifier.Function)
        }
        extensionFunctionDeclarations[functionSignature] = node
    }

    fun findExtensionFunction(name: String): FunctionDeclarationNode? {
        return extensionFunctionDeclarations[name] ?: parentScope?.findExtensionFunction(name)
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

    fun findClass(fullQualifiedName: String): Pair<ClassDefinition, SymbolTable>? {
        if (classDeclarations.containsKey(fullQualifiedName)) {
            return classDeclarations[fullQualifiedName]!! to this
        } else {
            return parentScope?.findClass(fullQualifiedName)
        }
    }

    fun mergeRuntimeSymbolTableIntoThis(other: SymbolTable) {
        other.propertyValues.forEach {
            putPropertyHolder(it.key, it.value)
        }
        other.functionDeclarations.forEach {
            declareFunction(it.key, it.value)
        }
        other.classDeclarations.forEach {
            declareClass(it.value)
        }
    }

    override fun toString(): String {
        return "functionDeclarations = $functionDeclarations\n" +
                "propertyDeclarations = $propertyDeclarations\n" +
                "propertyValues = $propertyValues"
    }

}
