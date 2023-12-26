package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.error.DuplicateIdentifierException
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier

class SymbolTable(val scopeLevel: Int, val scopeName: String, val scopeType: ScopeType, val parentScope: SymbolTable?) {
    private val propertyDeclarations = mutableMapOf<String, TypeNode>()
    internal val propertyValues = mutableMapOf<String, RuntimeValue>()

    private val functionDeclarations = mutableMapOf<String, FunctionDeclarationNode>()

    fun declareProperty(name: String, type: TypeNode) {
        if (hasProperty(name = name, true)) {
            throw DuplicateIdentifierException(name = name, classifier = IdentifierClassifier.Property)
        }
        propertyDeclarations[name] = type
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

    fun read(name: String): RuntimeValue {
        return propertyValues[name] ?: parentScope?.read(name) ?: throw RuntimeException("The variable `$name` has not been declared")
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

    fun findPropertyByDeclaredName(declaredName: String): RuntimeValue? {
        return propertyValues.keys.firstOrNull { it.substring(0 ..< it.lastIndexOf('/')) == declaredName }
            ?.let { transformedName -> propertyValues[transformedName] }
    }

    override fun toString(): String {
        return "functionDeclarations = $functionDeclarations\n" +
                "propertyDeclarations = $propertyDeclarations\n" +
                "propertyValues = $propertyValues"
    }
}
