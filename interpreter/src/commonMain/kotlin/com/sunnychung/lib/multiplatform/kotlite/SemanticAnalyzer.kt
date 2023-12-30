package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
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
import com.sunnychung.lib.multiplatform.kotlite.model.SemanticDummyRuntimeValue
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

    /**
     * This method is stateful and modifies data.
     */
    fun checkPropertyWriteAccess(name: String): Int {
        if (!currentScope.hasProperty(name)) { // FIXME
            throw SemanticException("Property `$name` is not declared")
        }
        var scope = currentScope
        while (!scope.hasProperty(name, isThisScopeOnly = true)) {
            scope = scope.parentScope!!
        }

        val propertyType = scope.getPropertyType(name)
        if (!propertyType.isMutable && scope.hasAssignedInThisScope(name)) {
            throw SemanticException("val `$name` cannot be reassigned")
        }
        scope.assign(name, SemanticDummyRuntimeValue(propertyType.type))

        return scope.scopeLevel
    }

    fun ScriptNode.visit() {
        nodes.forEach { it.visit() }
    }

    fun UnaryOpNode.visit() {
        node!!.visit()
        if (operator in setOf("pre++", "pre--", "post++", "post--") && node is VariableReferenceNode) {
            checkPropertyWriteAccess((node as VariableReferenceNode).variableName)
        }
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
        currentScope.declareProperty(name = name, type = type, isMutable = isMutable)
        if (initialValue != null) {
            currentScope.assign(name, SemanticDummyRuntimeValue(currentScope.getPropertyType(name).type))
        }
        transformedRefName = "$name/${scopeLevel}"
    }

    fun VariableReferenceNode.visit() {
        val l = checkPropertyReadAccess(variableName)
        if (variableName != "this" && transformedRefName == null) {
            transformedRefName = "$variableName/$l"
            currentScope.findPropertyOwner(transformedRefName!!)?.let {
                ownerRef = it
            }
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
        when (function) {
            is VariableReferenceNode -> {
                val name = function.variableName
                val functionNode = currentScope.findFunction(name)
                if (functionNode == null) {
                    currentScope.findClass(name) ?: throw SemanticException("Function $name not found")
                }
            }

            is NavigationNode -> {
                function.visit()
                // TODO
            }

            else -> throw UnsupportedOperationException("Dynamic functions are not yet supported")
        }

        arguments.forEach { it.visit() }
        // FIXME
    }

    fun FunctionValueParameterNode.visit() {
        defaultValue?.visit()
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException("Property `$name` has already been declared")
        }
        currentScope.declareProperty(name = name, type = type, isMutable = false)
        currentScope.assign(name, SemanticDummyRuntimeValue(currentScope.typeNodeToPropertyType(type, false)!!.type))
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

        // at this moment subject must not be a primitive
        member.visit()
    }

    fun ClassDeclarationNode.visit() {
        pushScope(name, ScopeType.Class)
        run {
            primaryConstructor?.visit()
            val nonPropertyArguments = primaryConstructor?.parameters
                ?.filter { !it.isProperty }
                ?.map { it.parameter }
            primaryConstructor?.parameters?.forEach {
                currentScope.undeclareProperty(it.parameter.name)
            }
            primaryConstructor?.parameters
                ?.filter { it.isProperty }
                ?.forEach {
                    val p = it.parameter
                    currentScope.declareProperty(name = p.name, type = p.type, isMutable = it.isMutable)
                    currentScope.assign(p.name, SemanticDummyRuntimeValue(currentScope.getPropertyType(p.name).type))
                }

            currentScope.parentScope!!.declareClass(
                ClassDefinition(
                    currentScope = currentScope.parentScope!!,
                    name = name,
                    fullQualifiedName = fullQualifiedName,
                    isInstanceCreationAllowed = true,
                    primaryConstructor = primaryConstructor,
                    rawMemberProperties = ((primaryConstructor?.parameters
                        ?.filter { it.isProperty }
                        ?.map {
                            val p = it.parameter
                            PropertyDeclarationNode(
                                name = p.name,
                                type = p.type,
                                isMutable = it.isMutable,
                                initialValue = p.defaultValue,
                                transformedRefName = p.transformedRefName,
                            )
                        } ?: emptyList()) +
                            declarations.filterIsInstance<PropertyDeclarationNode>()),
                    memberFunctions = declarations
                        .filterIsInstance<FunctionDeclarationNode>()
                        .associateBy { it.name },
                    orderedInitializersAndPropertyDeclarations = declarations
                        .filter { it is ClassInstanceInitializerNode || it is PropertyDeclarationNode },
                )
            )

            currentScope.declareProperty("this", TypeNode(name, null, false), false)
            primaryConstructor?.parameters
                ?.filter { it.isProperty }
                ?.map { it.parameter }
                ?.forEach { currentScope.declarePropertyOwner(it.transformedRefName!!, "this") }
            declarations.filter { it is ClassInstanceInitializerNode || it is PropertyDeclarationNode }
                .forEach {
                    pushScope("init-property", ScopeType.ClassInitializer)
                    nonPropertyArguments?.forEach { it.copy().visit() }
                    pushScope("init-property-inner", ScopeType.ClassInitializer)
                    it.visit()
                    popScope()
                    popScope()
                    if (it is PropertyDeclarationNode) {
                        it.visit(isVisitInitialValue = false)
                        currentScope.declarePropertyOwner(it.transformedRefName!!, "this")
                    }
                }

            declarations.filterIsInstance<FunctionDeclarationNode>()
                .forEach {
                    it.visit()
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
        // TODO check for write access
        // check for existence and return error
        try {
            val l = checkPropertyReadAccess(name)
            if (transformedRefName == null) {
                transformedRefName = "$name/$l"
            }
        } catch (_: SemanticException) {}
    }

    fun analyze() = scriptNode.visit()
}
