package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.NavigationNode
import com.sunnychung.lib.multiplatform.kotlite.model.NullNode
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
            is NullNode -> {}
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
            is ClassDeclarationNode -> this.visit()
            is ClassInstanceInitializerNode -> this.visit()
            is ClassMemberReferenceNode -> { /* TODO */ }
            is ClassParameterNode -> this.visit()
            is ClassPrimaryConstructorNode -> this.visit()
            is NavigationNode -> this.visit()
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
        if (subject !is VariableReferenceNode && subject !is NavigationNode) {
            throw SemanticException("$subject cannot be assigned")
        }
        value.visit()
        when (subject) {
            is VariableReferenceNode -> {
                subject.visit()
                val variableName = subject.variableName
                val l = checkPropertyWriteAccess(variableName)
                if (transformedRefName == null) {
                    transformedRefName = "$variableName/$l"
                }
            }
            is NavigationNode -> {
                subject.visit()
                // TODO handle NavigationNode
            }
            else -> SemanticException("$subject cannot be assigned")
        }
    }

    fun PropertyDeclarationNode.visit(isVisitInitialValue: Boolean = true, scopeLevel: Int = currentScope.scopeLevel) {
        if (isVisitInitialValue) {
            initialValue?.visit()
        }
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException("Property `$name` has already been declared")
        }
        currentScope.declareProperty(name = name, type = type)
        transformedRefName = "$name/${scopeLevel}"
    }

    fun VariableReferenceNode.visit() {
        val l = checkPropertyReadAccess(variableName)
        if (transformedRefName == null) {
            transformedRefName = "$variableName/$l"
        }
    }

    fun pushScope(scopeName: String, scopeType: ScopeType, overrideScopeLevel: Int = currentScope.scopeLevel + 1) {
        currentScope = SymbolTable(
            scopeLevel = overrideScopeLevel,
            scopeName = scopeName,
            scopeType = scopeType,
            parentScope = currentScope
        )
    }

    fun popScope() {
        currentScope = currentScope.parentScope!!
    }

    fun FunctionDeclarationNode.visit() {
        val previousScope = currentScope
        pushScope(
            scopeName = name,
            scopeType = ScopeType.Function,
        )

        valueParameters.forEach { it.visit() }
        previousScope.declareFunction(name, this)

        body.visit()

        popScope()
    }

    fun FunctionCallNode.visit() {
        val functionName = (function as? VariableReferenceNode ?: throw UnsupportedOperationException("Dynamic functions are not yet supported")).variableName
//        val functionNode = currentScope.findFunction(functionName) ?: throw SemanticException("Function $functionName not found")
        arguments.forEach { it.visit() }
        // FIXME
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
        pushScope(
            scopeName = "<block>",
            scopeType = type,
        )

        statements.forEach { it.visit() }

        popScope()
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

    fun NavigationNode.visit() {
        // TODO visit member property
        subject.visit()
    }

    fun ClassDeclarationNode.visit() {
        pushScope(name, ScopeType.Class)
        run {
            primaryConstructor?.visit()
            val nonPropertyArguments = primaryConstructor?.parameters
                ?.filter { !it.isProperty }
                ?.map { it.parameter }
            nonPropertyArguments?.forEach {
                currentScope.undeclareProperty(it.name)
            }

            currentScope.declareProperty("this", TypeNode("", null, false))
            declarations.filter { it is ClassInstanceInitializerNode || it is PropertyDeclarationNode }
                .forEach {
                    pushScope("init-property", ScopeType.ClassInitializer, currentScope.scopeLevel /* not to increase */)
                    nonPropertyArguments?.forEach { it.visit() }
                    pushScope("init-property-inner", ScopeType.ClassInitializer)
                    it.visit()
                    popScope()
                    popScope()
                    if (it is PropertyDeclarationNode) {
                        it.visit(isVisitInitialValue = false)
                    }
                }
        }
        popScope()
    }

    fun ClassPrimaryConstructorNode.visit() {
        parameters.forEach { it.visit() }
    }

    fun ClassParameterNode.visit() {
        parameter.visit()
    }

    fun ClassInstanceInitializerNode.visit() {
        block.visit()
    }

    fun ClassMemberReferenceNode.visit() {

    }

    fun analyze() = scriptNode.visit()
}
