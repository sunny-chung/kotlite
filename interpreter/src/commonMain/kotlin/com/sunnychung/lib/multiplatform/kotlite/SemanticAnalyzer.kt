package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType.Companion.isLoop
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode

class SemanticAnalyzer(val scriptNode: ScriptNode) {
    val symbolTable = SymbolTable(scopeLevel = 1, scopeName = ":global", scopeType = ScopeType.Script, parentScope = null)
    var currentScope = symbolTable

    fun ASTNode.visit() {
        when (this) {
            is AssignmentNode -> this.visit()
            is BinaryOpNode -> this.visit()
            is FunctionDeclarationNode -> this.visit()
            is FunctionValueParameterNode -> this.visit()
            is IntegerNode -> {}
            is DoubleNode -> {}
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
            is BreakNode -> this.visit()
            is ContinueNode -> this.visit()
            is IfNode -> this.visit()
            is WhileNode -> this.visit()
        }
    }

    fun checkPropertyReadAccess(name: String): Int {
        if (!currentScope.hasProperty(name)) {
            throw SemanticException("Property `$name` is not declared")
        }
        var scope = currentScope
        while (!scope.hasProperty(name, isThisScopeOnly = true)) {
            scope = scope.parentScope!!
        }
        return scope.scopeLevel
    }

    fun checkPropertyWriteAccess(name: String): Int {
        if (!currentScope.hasProperty(name)) { // FIXME
            throw SemanticException("Property `$name` is not declared")
        }
        var scope = currentScope
        while (!scope.hasProperty(name, isThisScopeOnly = true)) {
            scope = scope.parentScope!!
        }
        return scope.scopeLevel
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
        val l = checkPropertyWriteAccess(variableName)
        if (transformedRefName == null) {
            transformedRefName = "$variableName/$l"
        }
    }

    fun PropertyDeclarationNode.visit() {
        initialValue?.visit()
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException("Property `$name` has already been declared")
        }
        currentScope.declareProperty(name = name, type = type)
        transformedRefName = "$name/${currentScope.scopeLevel}"
    }

    fun VariableReferenceNode.visit() {
        val l = checkPropertyReadAccess(variableName)
        if (transformedRefName == null) {
            transformedRefName = "$variableName/$l"
        }
    }

    fun FunctionDeclarationNode.visit() {
        val previousScope = currentScope
        currentScope = SymbolTable(
            scopeLevel = previousScope.scopeLevel + 1,
            scopeName = name,
            scopeType = ScopeType.Function,
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
        transformedRefName = "$name/${currentScope.scopeLevel}"
    }

    fun FunctionCallArgumentNode.visit() {
        value.visit()
    }

    fun BlockNode.visit() {
        val previousScope = currentScope
        currentScope = SymbolTable(
            scopeLevel = previousScope.scopeLevel + 1,
            scopeName = "<block>",
            scopeType = type,
            parentScope = previousScope
        )

        statements.forEach { it.visit() }

        currentScope = previousScope
    }

    fun ReturnNode.visit() {
        value?.visit()
        var s = currentScope
        while (s.scopeType != ScopeType.Function) {
            if (s.scopeType == ScopeType.Script || s.parentScope == null) {
                throw SemanticException("`return` statement should be within a function")
            }
            s = s.parentScope!!
        }
    }

    fun checkBreakOrContinueScope() {
        var s = currentScope
        while (!s.scopeType.isLoop()) {
            if (s.scopeType in setOf(ScopeType.Script, ScopeType.Function) || s.parentScope == null) {
                throw SemanticException("`break` statement should be within a function")
            }
            s = s.parentScope!!
        }
    }

    fun BreakNode.visit() {
        checkBreakOrContinueScope()
    }

    fun ContinueNode.visit() {
        checkBreakOrContinueScope()
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
