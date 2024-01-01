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
    internal val propertyValues = mutableMapOf<String, RuntimeValue>()
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

    fun typeNodeToPropertyType(type: TypeNode, isMutable: Boolean): PropertyType? {
        val primitiveType = type.toPrimitiveDataType()
        val dataType = primitiveType ?: ObjectType(clazz = findClass(type.name) ?: return null, isNullable = type.isNullable)
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
            if (!type.isMutable && propertyValues.containsKey(name)) {
                throw RuntimeException("val cannot be reassigned")
            }
            if (!type.type.isAssignableFrom(value.type())) {
                throw RuntimeException("Type ${value.type().name} cannot be casted to ${type.type.name}")
            }
            propertyValues[name] = value
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

    fun getPropertyType(name: String, isThisScopeOnly: Boolean = false): PropertyType {
        return propertyDeclarations[name]
            ?: Unit.takeIf { isThisScopeOnly }.let { parentScope?.getPropertyType(name) }
            ?: throw RuntimeException("The variable `$name` has not been declared")
    }

    fun read(name: String, isThisScopeOnly: Boolean = false): RuntimeValue {
        return propertyValues[name]
            ?: Unit.takeIf { isThisScopeOnly }.let { parentScope?.read(name) }
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

    fun findFunction(name: String): FunctionDeclarationNode? {
        return functionDeclarations[name] ?: parentScope?.findFunction(name)
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
            .let { transformedName -> propertyValues[transformedName] }
    }

    fun declareClass(classDefinition: ClassDefinition) {
        if (findClass(classDefinition.fullQualifiedName) != null) {
            throw DuplicateIdentifierException(name = classDefinition.fullQualifiedName, classifier = IdentifierClassifier.Class)
        }
        classDeclarations[classDefinition.fullQualifiedName] = classDefinition
    }

    fun findClass(fullQualifiedName: String): ClassDefinition? {
        if (classDeclarations.containsKey(fullQualifiedName)) {
            return classDeclarations[fullQualifiedName]
        } else {
            return parentScope?.findClass(fullQualifiedName)
        }
    }

    override fun toString(): String {
        return "functionDeclarations = $functionDeclarations\n" +
                "propertyDeclarations = $propertyDeclarations\n" +
                "propertyValues = $propertyValues"
    }

}
