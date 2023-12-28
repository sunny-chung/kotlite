package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.error.DuplicateIdentifierException
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier

class SymbolTable(val scopeLevel: Int, val scopeName: String, val scopeType: ScopeType, val parentScope: SymbolTable?) {
    private val propertyDeclarations = mutableMapOf<String, TypeNode>()
    internal val propertyValues = mutableMapOf<String, RuntimeValue>()

    private val functionDeclarations = mutableMapOf<String, FunctionDeclarationNode>()

    private val classDeclarations = mutableMapOf<String, ClassDefinition>()

    fun declareProperty(name: String, type: TypeNode) {
        if (hasProperty(name = name, true)) {
            throw DuplicateIdentifierException(name = name, classifier = IdentifierClassifier.Property)
        }
        propertyDeclarations[name] = type
    }

    fun undeclareProperty(name: String) {
        if (!hasProperty(name = name, true)) {
            throw RuntimeException("No such property `$name`")
        }
        propertyDeclarations.remove(name)
    }

    fun undeclarePropertyByDeclaredName(declaredName: String) {
        undeclareProperty(findTransformedNameByDeclaredName(declaredName))
    }

    fun assign(name: String, value: RuntimeValue): Boolean { // TODO check type, modifiable
        if (propertyDeclarations.containsKey(name)) {
            propertyValues[name] = value
            return true
        } else if (parentScope?.assign(name, value) == true) {
            return true
        } else {
            throw RuntimeException("The variable `$name` has not been declared")
        }
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
