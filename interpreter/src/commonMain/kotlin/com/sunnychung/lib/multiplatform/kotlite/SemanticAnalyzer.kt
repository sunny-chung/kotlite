package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode

class SemanticAnalyzer(val scriptNode: ScriptNode) {
    val symbolTable = SymbolTable(scopeLevel = 1, scopeName = ":global", parentScope = null)
    var currentScope = symbolTable

    fun ASTNode.visit() {
        when (this) {
            is AssignmentNode -> this.visit()
            is BinaryOpNode -> this.visit()
            is FunctionDeclarationNode -> this.visit()
            is FunctionValueParameterNode -> this.visit()
            is IntegerNode -> {}
            is BooleanNode -> {}
            is PropertyDeclarationNode -> this.visit()
            is ScriptNode -> this.visit()
            is TypeNode -> {}
            is UnaryOpNode -> this.visit()
            is VariableReferenceNode -> this.visit()
            is FunctionCallArgumentNode -> this.visit()
            is FunctionCallNode -> this.visit()
            is BlockNode -> this.visit()
            is ReturnNode -> this.visit()
            is BreakNode -> {}
            is ContinueNode -> {}
            is IfNode -> this.visit()
            is WhileNode -> this.visit()
        }
    }

    fun checkPropertyReadAccess(name: String) {
        if (!currentScope.hasProperty(name)) {
            throw SemanticException("Property `$name` is not declared")
        }
    }

    fun checkPropertyWriteAccess(name: String) {
        if (!currentScope.hasProperty(name)) { // FIXME
            throw SemanticException("Property `$name` is not declared")
        }
    }

    fun ScriptNode.visit() {
        nodes.forEach { it.visit() }
    }

    fun UnaryOpNode.visit() {
        node!!.visit()
    }

    fun BinaryOpNode.visit() {
        node1.visit()
        node2.visit()
    }

    fun AssignmentNode.visit() {
        value.visit()
        checkPropertyWriteAccess(variableName)
    }

    fun PropertyDeclarationNode.visit() {
        initialValue?.visit()
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException("Property `$name` has already been declared")
        }
        currentScope.declareProperty(name = name, type = type)
    }

    fun VariableReferenceNode.visit() {
        checkPropertyReadAccess(variableName)
    }

    fun FunctionDeclarationNode.visit() {
        val previousScope = currentScope
        currentScope = SymbolTable(
            scopeLevel = previousScope.scopeLevel + 1,
            scopeName = name,
            parentScope = previousScope
        )

        valueParameters.forEach { it.visit() }
        previousScope.declareFunction(name, this)

        body.visit()

        currentScope = previousScope
    }

    fun FunctionCallNode.visit() {
        val functionName = (function as? VariableReferenceNode ?: throw UnsupportedOperationException("Dynamic functions are not yet supported")).variableName
        val functionNode = currentScope.findFunction(functionName) ?: throw SemanticException("Function $functionName not found")
        arguments.forEach { it.visit() }
    }

    fun FunctionValueParameterNode.visit() {
        defaultValue?.visit()
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException("Property `$name` has already been declared")
        }
        currentScope.declareProperty(name = name, type = type)
    }

    fun FunctionCallArgumentNode.visit() {
        value.visit()
    }

    fun BlockNode.visit() {
        val previousScope = currentScope
        currentScope = SymbolTable(
            scopeLevel = previousScope.scopeLevel + 1,
            scopeName = "<block>",
            parentScope = previousScope
        )

        statements.forEach { it.visit() }

        currentScope = previousScope
    }

    fun ReturnNode.visit() {
        value?.visit()
    }

    fun IfNode.visit() {
        condition.visit()
        trueBlock?.visit()
        falseBlock?.visit()
    }

    fun WhileNode.visit() {
        condition.visit()
        body?.visit()
    }

    fun analyze() = scriptNode.visit()
}
