package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.CannotInferTypeException
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.error.TypeMismatchException
import com.sunnychung.lib.multiplatform.kotlite.extension.emptyToNull
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterType
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeArguments
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeToUpperBound
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AnyType
import com.sunnychung.lib.multiplatform.kotlite.model.AsOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.CallableType
import com.sunnychung.lib.multiplatform.kotlite.model.CatchNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharType
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassModifier
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleType
import com.sunnychung.lib.multiplatform.kotlite.model.ElvisOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionBodyFormat
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentInfo
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionType
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IndexOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.InfixFunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntType
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.LabelNode
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.LongNode
import com.sunnychung.lib.multiplatform.kotlite.model.LongType
import com.sunnychung.lib.multiplatform.kotlite.model.NavigationNode
import com.sunnychung.lib.multiplatform.kotlite.model.NullNode
import com.sunnychung.lib.multiplatform.kotlite.model.NothingType
import com.sunnychung.lib.multiplatform.kotlite.model.ObjectType
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyAccessorsNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyModifier
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyOwnerInfo
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType.Companion.isLoop
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.SearchFunctionModifier
import com.sunnychung.lib.multiplatform.kotlite.model.SemanticAnalyzerSymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.SemanticDummyRuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringType
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolReferenceSet
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.ThrowNode
import com.sunnychung.lib.multiplatform.kotlite.model.TryNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterType
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.ValueNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenConditionNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenEntryNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenSubjectNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode
import com.sunnychung.lib.multiplatform.kotlite.model.isNonNullIntegralType
import com.sunnychung.lib.multiplatform.kotlite.model.isNonNullNumberType
import com.sunnychung.lib.multiplatform.kotlite.model.toSignature
import com.sunnychung.lib.multiplatform.kotlite.util.ClassMemberResolver

class SemanticAnalyzer(val scriptNode: ScriptNode, executionEnvironment: ExecutionEnvironment) {
    val builtinSymbolTable = SemanticAnalyzerSymbolTable(scopeLevel = 0, scopeName = ":builtin", scopeType = ScopeType.Script, parentScope = null)
    val symbolTable = SemanticAnalyzerSymbolTable(scopeLevel = 1, scopeName = ":global", scopeType = ScopeType.Script, parentScope = builtinSymbolTable)
    var currentScope = builtinSymbolTable
    var functionDefIndex = 0
    val symbolRecorders = mutableListOf<SymbolReferenceSet>()

    // a cache of common types for optimization. not a must to use them
    val typeRegistry = listOf(
        TypeNode(SourcePosition.NONE, "Any", null, false),
        TypeNode(SourcePosition.NONE, "Int", null, false),
        TypeNode(SourcePosition.NONE, "Long", null, false),
        TypeNode(SourcePosition.NONE, "Double", null, false),
        TypeNode(SourcePosition.NONE, "Boolean", null, false),
        TypeNode(SourcePosition.NONE, "String", null, false),
        TypeNode(SourcePosition.NONE, "Char", null, false),
        TypeNode(SourcePosition.NONE, "Unit", null, false),
        TypeNode(SourcePosition.NONE, "Throwable", null, false),
    )
        .flatMap {
            listOf(
                it.name to it,
                "${it.name}?" to it.copy(isNullable = true),
            )
        }
        .let { it + listOf(
            "Null" to TypeNode(SourcePosition.NONE, "Nothing", null, true),
            "Nothing" to TypeNode(SourcePosition.NONE, "Nothing", null, false),
        ) }
        .toMap()

    val supportedOperatorFunctionNames = setOf("get", "set")

    init {
        executionEnvironment.getBuiltinClasses(builtinSymbolTable).forEach {
            builtinSymbolTable.declareClass(SourcePosition.BUILTIN, it)
        }

        executionEnvironment.getExtensionProperties(builtinSymbolTable).forEach {
            Parser(Lexer("TODO", it.type)).type().also { type -> // TODO filename
                it.typeNode = type
            }
            it.transformedName = "EP//${it.receiver}/${it.declaredName}/${++functionDefIndex}"
            builtinSymbolTable.declareExtensionProperty(SourcePosition.BUILTIN, it.transformedName!!, it)
        }
        val libFunctions = executionEnvironment.getBuiltinFunctions(builtinSymbolTable)
        libFunctions.forEach {
            it.visit()
        }

        currentScope = symbolTable
    }

    fun TypeNode.toNullable() = if (isNullable) {
        this
    } else {
        typeRegistry["$name?"] ?: this.copy(isNullable = true)
    }

    fun DataType.toTypeNode(): TypeNode =
        if (this is NothingType) {
            typeRegistry["Null"]!!
        } else if (this !is ObjectType && this !is FunctionType && this !is TypeParameterType) {
            typeRegistry["$name${if (isNullable) "?" else ""}"]!!
        } else if (this is FunctionType) {
            FunctionTypeNode(
                position = SourcePosition.NONE,
                parameterTypes = arguments.map { it.toTypeNode() },
                returnType = returnType.toTypeNode(),
                isNullable = isNullable,
            )
        } else if (this is ObjectType) {
            TypeNode(SourcePosition.NONE, name, arguments.map { it.toTypeNode() }.emptyToNull(), isNullable)
        } else {
            TypeNode(SourcePosition.NONE, name, null, isNullable)
        }

    fun TypeNode.toDataType(): DataType {
        return try {
            currentScope.assertToDataType(this)
        } catch (e: SemanticException) {
            throw e
        } catch (e: RuntimeException) {
            e.printStackTrace()
            throw SemanticException(position, e.message ?: e.toString())
        }
    }

    fun TypeNode.unboxClassTypeAsCompanion() = if (this is ClassTypeNode) {
        TypeNode(position, "${this.clazz.name}.Companion", this.clazz.arguments, false)
    } else {
        this
    }

    fun DataType.resolveTypeParameterAsUpperBound(): DataType {
        var subjectType = this
        while (subjectType is TypeParameterType) {
            if (subjectType == subjectType.upperBound) {
                throw RuntimeException("subjectType upper bound is itself")
            }
            subjectType = subjectType.upperBound
        }
        return subjectType
    }

    protected fun isLocalAndNotCurrentScope(scopeLevel: Int): Boolean {
        return scopeLevel > 1 && scopeLevel <= (symbolRecorders.lastOrNull()?.scopeLevel ?: currentScope.scopeLevel)
    }

    fun temporarilySwitchToScopeAndRun(scope: SemanticAnalyzerSymbolTable, executable: () -> Unit) {
        val oldScope = currentScope
        currentScope = scope
        try {
            executable()
        } finally {
            currentScope = oldScope
        }
    }

    data class Modifier(
        /**
         * This would skip visiting lambdas.
         */
        val isSkipGenerics: Boolean = false,
    )

    fun ASTNode.visit(modifier: Modifier = Modifier()) {
        when (this) {
            is AssignmentNode -> this.visit(modifier = modifier)
            is BinaryOpNode -> this.visit(modifier = modifier)
            is FunctionDeclarationNode -> this.visit(modifier = modifier)
            is FunctionValueParameterNode -> TODO() //this.visit(modifier = modifier)
            is IntegerNode -> {}
            is LongNode -> {}
            is DoubleNode -> {}
            is BooleanNode -> {}
            is NullNode -> {}
            is PropertyDeclarationNode -> this.visit(modifier = modifier)
            is ScriptNode -> this.visit(modifier = modifier)
            is TypeNode -> this.visit(modifier = modifier)
            is TypeParameterNode -> TODO()
            is UnaryOpNode -> this.visit(modifier = modifier)
            is VariableReferenceNode -> this.visit(modifier = modifier)
            is FunctionCallArgumentNode -> this.visit(modifier = modifier)
            is FunctionCallNode -> this.visit(modifier = modifier)
            is BlockNode -> this.visit(modifier = modifier)
            is ReturnNode -> this.visit(modifier = modifier)
            is BreakNode -> this.visit(modifier = modifier)
            is ContinueNode -> this.visit(modifier = modifier)
            is IfNode -> this.visit(modifier = modifier)
            is WhileNode -> this.visit(modifier = modifier)
            is ClassDeclarationNode -> this.visit(modifier = modifier)
            is ClassInstanceInitializerNode -> this.visit(modifier = modifier)
            is ClassMemberReferenceNode -> { /* TODO */ }
            is ClassParameterNode -> this.visit(modifier = modifier)
            is ClassPrimaryConstructorNode -> this.visit(modifier = modifier)
            is NavigationNode -> this.visit(modifier = modifier)
            is IndexOpNode -> this.visit(modifier = modifier)
            is PropertyAccessorsNode -> TODO()
            is ValueNode -> {}
            is StringLiteralNode -> {}
            is StringNode -> this.visit(modifier = modifier)
            is LambdaLiteralNode -> this.visit(modifier = modifier)
            is CharNode -> {}
            is AsOpNode -> this.visit(modifier = modifier)
            is InfixFunctionCallNode -> this.visit(modifier = modifier)
            is ElvisOpNode -> this.visit(modifier = modifier)
            is ThrowNode -> this.visit(modifier = modifier)
            is CatchNode -> this.visit(modifier = modifier)
            is TryNode -> this.visit(modifier = modifier)
            is WhenConditionNode -> this.visit(modifier = modifier)
            is WhenEntryNode -> this.visit(modifier = modifier)
            is WhenNode -> this.visit(modifier = modifier)
            is WhenSubjectNode -> this.visit(modifier = modifier)
            is LabelNode -> TODO()
        }
    }

    fun checkPropertyReadAccess(accessNode: ASTNode, name: String): Int {
        if (!currentScope.hasProperty(name)) {
            throw SemanticException(accessNode.position, "Property `$name` is not declared")
        }
        var scope: SymbolTable = currentScope
        while (!scope.hasProperty(name, isThisScopeOnly = true)) {
            scope = scope.parentScope!!
        }
        return scope.scopeLevel
    }

    /**
     * This method is stateful and modifies data.
     */
    fun checkPropertyWriteAccess(accessNode: ASTNode, name: String): Int {
        if (!currentScope.hasProperty(name)) {
            throw SemanticException(accessNode.position, "Property `$name` is not declared")
        }
        var scope: SymbolTable = currentScope
        while (!scope.hasProperty(name, isThisScopeOnly = true)) {
            scope = scope.parentScope!!
        }

        val propertyType = scope.getPropertyType(name).first
        if (!propertyType.isMutable && scope.hasAssignedInThisScope(name)) {
            throw SemanticException(accessNode.position, "val `$name` cannot be reassigned")
        }
        scope.assign(name, SemanticDummyRuntimeValue(propertyType.type))

        return scope.scopeLevel
    }

    fun findExtensionFunction(receiverType: DataType, functionName: String): FunctionDeclarationNode? {
//        return if (receiverType is ObjectType) {
//            val clazz = receiverType.clazz
//            currentScope.findExtensionFunction("${clazz.fullQualifiedName}/${functionName}")
//        } else {
//            currentScope.findExtensionFunction("${receiverType.name}/${functionName}")
//        }
        return currentScope.findExtensionFunctions(receiverType, functionName).firstOrNull()?.first
    }

    fun ScriptNode.visit(modifier: Modifier = Modifier()) {
        nodes.forEach { it.visit(modifier = modifier) }
    }

    fun TypeNode.visit(modifier: Modifier = Modifier()) {
        try {
            currentScope.typeNodeToDataType(this)
        } catch (e: RuntimeException) {
            throw SemanticException(position, e.message ?: e.toString() ?: "")
        }
            ?: throw SemanticException(position, "Unknown type `${this.descriptiveName()}`")
    }

    fun UnaryOpNode.visit(modifier: Modifier = Modifier()) {
        node!!.visit(modifier = modifier)
        if (operator in setOf("pre++", "pre--", "post++", "post--") && node is VariableReferenceNode) {
            checkPropertyWriteAccess(this, (node as VariableReferenceNode).variableName)
        }
    }

    fun BinaryOpNode.visit(modifier: Modifier = Modifier()) {
        node1.visit(modifier = modifier)
        node2.visit(modifier = modifier)
        type()
    }

    fun AssignmentNode.visit(modifier: Modifier = Modifier()) {
        if (subject !is VariableReferenceNode && subject !is NavigationNode && subject !is IndexOpNode) {
            throw SemanticException(position, "$subject cannot be assigned")
        }
        when (subject) {
            is VariableReferenceNode -> {
                subject.visit(modifier = modifier)
                val variableName = subject.variableName
                val l = checkPropertyWriteAccess(this, variableName)
                if (transformedRefName == null) {
                    transformedRefName = "$variableName/$l"
                }

//                log.v { "Assign $variableName type ${subject.type()} <- type ${value.type()}" }
            }
            is NavigationNode -> {
                subject.visit(modifier = modifier, isCheckWriteAccess = true)
                // TODO handle NavigationNode

//                log.v { "Assign ${subject.member.name} type ${subject.type()} <- type ${value.type()}" }
            }
            is IndexOpNode -> {
                subject.visit(modifier = modifier, isWriteOnly = operator == "=")
                functionCall = FunctionCallNode(
                    /**
                     * e.g. `x[0] = v`, subject == x[0], subject.subject == x
                     */
                    function = NavigationNode(position, subject.subject, ".", ClassMemberReferenceNode(position, "set")),
                    arguments = subject.arguments.mapIndexed { index, it ->
                        FunctionCallArgumentNode(position = it.position, index = index, value = it)
                    } + listOf(FunctionCallArgumentNode(position = value.position, index = subject.arguments.size, value = value)),
                    declaredTypeArguments = emptyList(),
                    position = position,
                    modifierFilter = SearchFunctionModifier.OperatorFunctionOnly,
                )
                functionCall!!.visit(modifier)
            }
            else -> throw SemanticException(position, "$subject cannot be assigned")
        }
        val subjectRawType = subject.type()
        val subjectType = subjectRawType.toDataType()

        if (operator == "=" && subjectRawType is FunctionTypeNode && value is LambdaLiteralNode) {
            value.parameterTypesUpperBound = subjectRawType.parameterTypes
            value.returnTypeUpperBound = subjectRawType.returnType
        }

        value.visit(modifier = modifier)
        val valueType = value.type().toDataType()

        if (operator in setOf("+=", "-=", "*=", "/=", "%=")) {
            if (operator == "+=" && subjectType is StringType) {
                return // string can concat anything
            }
            if (!valueType.isNonNullNumberType()) {
                throw TypeMismatchException(position, "non-null number type", valueType.nameWithNullable)
            }
            if (subjectType is DoubleType) {
                return // ok
            }
            if (subjectType is LongType && valueType.isNonNullIntegralType()) {
                return // ok
            }
        }

        if (!subjectType.isAssignableFrom(valueType)) {
            throw TypeMismatchException(position, subjectType.nameWithNullable, valueType.nameWithNullable)
        }
    }

    fun evaluateAndRegisterReturnType(node: ASTNode) {
        fun setOfAllTypeNodes(type: TypeNode): Set<TypeNode> {
            return mutableSetOf(type) + (type.arguments?.flatMap { setOfAllTypeNodes(it) } ?: emptyList())
        }

        val type = node.type()
        if (symbolRecorders.isNotEmpty()) {
            val symbols = symbolRecorders.last()
            val typesToResolve = setOfAllTypeNodes(type)
            typesToResolve.forEach {
                val find = currentScope.findClass(it.name)
                if (find != null && isLocalAndNotCurrentScope(find.second.scopeLevel)) {
                    symbols.classes += find.first.fullQualifiedName
                } else {
                    val find = currentScope.findTypeAlias(it.name)
                    if (find != null && isLocalAndNotCurrentScope(find.second.scopeLevel)) {
                        symbols.typeAlias += it.name
                    }
                }
            }
        }
    }

    fun PropertyDeclarationNode.visit(modifier: Modifier = Modifier(), isVisitInitialValue: Boolean = true, isClassProperty: Boolean = false, scopeLevel: Int = currentScope.scopeLevel) {
        if (declaredModifiers.contains(PropertyModifier.override)) {
            inferredModifiers += PropertyModifier.open
        }
        if (isVisitInitialValue) {
            if (declaredType is FunctionTypeNode && initialValue is LambdaLiteralNode) {
                initialValue.parameterTypesUpperBound = declaredType.parameterTypes
                initialValue.returnTypeUpperBound = declaredType.returnType
            }
            initialValue?.visit(modifier = modifier)
        }
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException(position, "Property `$name` has already been declared")
        }
        if (!isClassProperty && accessors != null) {
            throw SemanticException(position, "Only class member properties can define custom accessors")
        }
//        if (isMutable && accessors != null) {
//            throw SemanticException("`var` with custom accessors is not supported")
//        }
//        if (accessors?.setter != null) {
//            throw SemanticException("Custom setter is currently not supported")
//        }
        if (isVisitInitialValue && accessors != null) {
            accessors.getter?.visit(modifier = modifier)
            accessors.setter?.visit(modifier = modifier)
        }
        if (initialValue != null) {
            val valueType = initialValue.type().toDataType()
            if (declaredType == null) {
                inferredType = initialValue.type()
            }
            val subjectType = type.toDataType()
            if (!subjectType.isAssignableFrom(valueType) && subjectType != valueType) {
                throw TypeMismatchException(position, subjectType.descriptiveName, valueType.descriptiveName)
            }
        } else if (declaredType == null) {
            throw SemanticException(position, "Type cannot be inferred for property `$name`")
        }
        currentScope.declareProperty(position = position, name = name, type = type, isMutable = isMutable)
        if (initialValue != null) {
            if (accessors != null) {
                throw SemanticException(position, "Property `$name` with an initial value cannot have custom accessors")
            }

            currentScope.assign(name, SemanticDummyRuntimeValue(currentScope.getPropertyType(name).first.type))
        }
        transformedRefName = "$name/${scopeLevel}"
        currentScope.registerTransformedSymbol(position, IdentifierClassifier.Property, transformedRefName!!, name)

        evaluateAndRegisterReturnType(this)
    }

    fun VariableReferenceNode.visit(modifier: Modifier = Modifier()) {
        if (!currentScope.hasProperty(variableName)) {
            if (currentScope.findClass(variableName) != null) {
                return
            }
        }
        val l = checkPropertyReadAccess(this, variableName)
        if (variableName != "this" && variableName != "super" && transformedRefName == null) {
            transformedRefName = "$variableName/$l"
            currentScope.findPropertyOwner(transformedRefName!!)?.let {
                ownerRef = it
            }
            if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(l)) {
                val symbols = symbolRecorders.last()
                symbols.properties += ownerRef?.ownerRefName ?: transformedRefName!!
            }
        } else if (variableName == "this" || variableName == "super") {
            if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(l)) {
                val symbols = symbolRecorders.last()
                symbols.properties += variableName
            }
        }

        evaluateAndRegisterReturnType(this)
    }

    fun pushScope(scopeName: String, scopeType: ScopeType, returnType: DataType? = null, overrideScopeLevel: Int = currentScope.scopeLevel + 1) {
        currentScope = SemanticAnalyzerSymbolTable(
            scopeLevel = overrideScopeLevel,
            scopeName = scopeName,
            scopeType = scopeType,
            returnType = returnType,
            parentScope = currentScope
        )
    }

    fun popScope() {
        currentScope = currentScope.parentScope!! as SemanticAnalyzerSymbolTable
    }

    fun FunctionDeclarationNode.visit(modifier: Modifier = Modifier(), isClassMemberFunction: Boolean = false) {
        val previousScope = currentScope
        var additionalScopeCount = 0
        var variantsOfThis = mutableListOf<FunctionDeclarationNode>()
        val isVararg = valueParameters.isNotEmpty() &&
            valueParameters.first().modifiers.contains(FunctionValueParameterModifier.vararg)

        if (isVararg && valueParameters.size > 1) {
            throw SemanticException(valueParameters[1].position, "Only exactly one function parameter is supported if there is a vararg parameter")
        }
        if (!isVararg && valueParameters.size > 1) {
            (1..< valueParameters.size).forEach { i ->
                if (valueParameters[i].modifiers.contains(FunctionValueParameterModifier.vararg)) {
                    throw SemanticException(valueParameters[i].position, "Only exactly one function parameter is supported if there is a vararg parameter")
                }
            }
        }
        if (isVararg && valueParameters.first().defaultValue != null) {
            throw SemanticException(valueParameters.first().defaultValue!!.position, "Vararg value argument with a default value is not supported")
        }
        this.isVararg = isVararg

        if (declaredModifiers.contains(FunctionModifier.override)) {
            inferredModifiers += FunctionModifier.open
        }

        if (modifiers.contains(FunctionModifier.operator)) {
            if (!isClassMemberFunction && receiver == null) {
                throw SemanticException(position, "Operator functions are only allowed as a class member or as an extension function")
            }
            if (name !in supportedOperatorFunctionNames) {
                throw SemanticException(position, "`$name` is not a supported operator function name. Supported names are: ${supportedOperatorFunctionNames.joinToString(", ")}")
            }
        }

        pushScope(
            scopeName = name,
            scopeType = ScopeType.ExtraWrap,
        )
        ++additionalScopeCount

        typeParameters.forEach {
            currentScope.declareTypeAlias(position, it.name, it.typeUpperBound)
        }

        pushScope(
            scopeName = name,
            scopeType = ScopeType.Function,
            returnType = declaredReturnType?.resolveGenericParameterType(typeParameters)?.toDataType()
                ?: typeRegistry["Any?"]!!.toDataType(),
        )
        ++additionalScopeCount

        val visitValueParameters = {
            valueParameters.forEach {
                it.visit(modifier = modifier, this)
            }
        }

        if (receiver == null) {
            visitValueParameters()

            previousScope.declareFunction(position, name, this)
            if (transformedRefName == null) { // class declaration can assign transformedRefName
                transformedRefName = "$name/${++functionDefIndex}"
            }
            previousScope.registerTransformedSymbol(position, IdentifierClassifier.Function, transformedRefName!!, name)
        } else {
            val typeNode = receiver
            currentScope.declareProperty(position = position, name = "this", type = typeNode, isMutable = false)
            currentScope.registerTransformedSymbol(position, IdentifierClassifier.Property, "this", "this")
            val clazz = currentScope.findClass(typeNode.name)?.first ?: throw SemanticException(position, "Class `${receiver.descriptiveName()}` not found")
            if (clazz.superClass != null) {
                val superClassTypeResolutions = ClassMemberResolver(clazz, typeNode.arguments).genericResolutions.let { it[it.lastIndex - 1] }.second
                val superClassType = TypeNode(
                    SourcePosition.NONE,
                    clazz.superClass.name,
                    clazz.superClass.typeParameters.map { tp -> superClassTypeResolutions[tp.name]!! }.emptyToNull(),
                    isNullable = false,
                )
                currentScope.declareProperty(position = position, name = "super", type = superClassType, isMutable = false)
                currentScope.registerTransformedSymbol(position, IdentifierClassifier.Property, "super", "super")
            }
            if (!typeNode.isNullable) {
                clazz.typeParameters.forEachIndexed { index, it ->
                    currentScope.declareTypeAlias(position, it.name, it.typeUpperBound)
                    if ((receiver.arguments?.get(index) ?: throw SemanticException(position, "Missing receiver type argument")).name != "*") {
                        currentScope.declareTypeAliasResolution(position, it.name, receiver.arguments!![index])
                    }
                }
                clazz.getAllMemberPropertiesExcludingCustomAccessors().forEach {
                    currentScope.declareProperty(
                        position = position,
                        name = it.key,
                        type = it.value.type.toTypeNode(),
                        isMutable = it.value.isMutable
                    )
                    currentScope.declarePropertyOwner(
                        name = "${it.key}/${currentScope.scopeLevel}",
                        owner = "this/${receiver.descriptiveName()}"
                    )
                }
                clazz.getAllMemberFunctions().forEach {
                    currentScope.declareFunction(position = position, name = it.key, node = it.value)
                    currentScope.declareFunctionOwner(name = it.key, function = it.value, owner = "this/${receiver.descriptiveName()}")
                }
            }
            currentScope.findExtensionPropertyByReceiver(typeNode.resolveGenericParameterTypeToUpperBound(clazz.typeParameters)).forEach {
                currentScope.declareProperty(
                    position = position,
                    name = it.second.declaredName,
                    type = it.second.typeNode!!,
                    isMutable = it.second.setter != null
                )
                currentScope.declarePropertyOwner(
                    name = "${it.second.declaredName}/${currentScope.scopeLevel}",
                    owner = "this/${receiver.descriptiveName()}",
                    extensionPropertyRef = it.first,
                )
            }

            pushScope("$name(valueParameters)", ScopeType.FunctionParameters)
            ++additionalScopeCount

            visitValueParameters()

            // 1. setting `transformedRefName` must be before `this.copy()`.
            // 2. `transformedRefName` should be identical among these extension functions, because
            //    only one implementation would be registered in Interpreter.
            transformedRefName = "${typeNode.descriptiveName()}/$name/${++functionDefIndex}"
            previousScope.declareExtensionFunction(position, "${typeNode.name}/$name", this.copy(receiver = receiver.copy(isNullable = false)).also { variantsOfThis += it })

            if (typeNode.isNullable) {
                previousScope.declareExtensionFunction(position, "${typeNode.name}?/$name", this)
//                transformedRefName = "${typeNode.name}?/$name/${++functionDefIndex}"

//                previousScope.declareExtensionFunction("Nothing/$name", this.copy(receiver = typeRegistry["Null"]).also { variantsOfThis += it })
//                transformedRefName = "Nothing/$name/${++functionDefIndex}"
            }
        }

        body.visit(modifier = modifier)

        // TODO check for return statement

        val valueType = body.type().toDataType()
        if (declaredReturnType == null && body.format == FunctionBodyFormat.Expression) {
            inferredReturnType = body.type()
            variantsOfThis.forEach { it.inferredReturnType = inferredReturnType }
        } else {
            val subjectType = returnType.resolveGenericParameterType(typeParameters).toDataType()
            if (subjectType !is UnitType && !subjectType.isAssignableFrom(valueType)) {
                throw TypeMismatchException(position, subjectType.nameWithNullable, valueType.nameWithNullable)
            }
        }

        while (additionalScopeCount-- > 0) {
            popScope()
        }

        evaluateAndRegisterReturnType(this)
    }

    fun FunctionCallNode.visit(modifier: Modifier = Modifier()) {
        arguments.forEachIndexed { i, _ ->
            arguments.forEachIndexed { j, _ ->
                if (i < j && arguments[i].name != null && arguments[j].name != null && arguments[i].name == arguments[j].name) {
                    throw SemanticException(arguments[j].position, "Duplicated argument ${arguments[i].name}")
                }
            }
        }

        if (modifierFilter == null) {
            modifierFilter = SearchFunctionModifier.NoRestriction
        }

        pushScope("func", ScopeType.Function)

        // visit argument must before evaluating type
        arguments.forEach {
            it.visit(modifier = modifier.copy(isSkipGenerics = true))
        }

        class FunctionInfo(val valueParameters: List<Any>, val typeParameters: List<TypeParameterNode>, val receiverType: TypeNode?, val returnType: TypeNode)

        var extraTypeResolutions = emptyMap<String, TypeNode>()

        val functionArgumentAndReturnTypeDeclarations = when (function) {
            is VariableReferenceNode /* f(x) or constructor */, is TypeNode /* Superclass constructor */ -> {
                val functionName = when (function) {
                    is VariableReferenceNode -> function.variableName
                    is TypeNode -> function.name
                    else -> throw UnsupportedOperationException()
                }
                val resolution = currentScope.findMatchingCallables(
                    currentSymbolTable = currentScope,
                    originalName = functionName,
                    receiverType = null,
                    arguments = arguments.map { FunctionCallArgumentInfo(it.name, it.type(ResolveTypeModifier(isSkipGenerics = true)).toDataType()) },
                    modifierFilter = if (function is TypeNode) SearchFunctionModifier.ConstructorOnly else modifierFilter!!,
                )
                    .firstOrNull()
                    ?: throw SemanticException(position, "No matching function `${functionName}` found")
                if (function is VariableReferenceNode) {
                    function.ownerRef = resolution.owner?.let { PropertyOwnerInfo(it) }
                }
                functionRefName = resolution.transformedName
                callableType = resolution.type

                if (callableType == CallableType.Constructor) {
                    val clazz = resolution.definition as ClassDefinition
                    if (!clazz.isInstanceCreationAllowed) {
                        throw SemanticException(position, "Instances of class ${clazz.fullQualifiedName} cannot be created directly via constructor")
                    }
                }

                if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(resolution.scope.scopeLevel)) {
                    val symbols = symbolRecorders.last()
                    when (resolution.type) {
                        CallableType.Function, CallableType.Property -> {
                            if (resolution.owner == null) {
                                symbols.functions += resolution.transformedName
                            } else {
                                symbols.properties += resolution.owner
                            }
                        }

                        CallableType.Constructor -> {
                            symbols.classes += (resolution.definition as ClassDefinition).fullQualifiedName
                        }

                        CallableType.ExtensionFunction -> {}
                        CallableType.ClassMemberFunction -> {}
                    }
                }

                FunctionInfo(
                    valueParameters = resolution.arguments,
                    typeParameters = resolution.typeParameters,
                    receiverType = null,
                    returnType = resolution.returnType.let {
                        if (callableType == CallableType.Constructor) {
                            TypeNode(SourcePosition.NONE, it.name, resolution.typeParameters.map {
                                TypeNode(SourcePosition.NONE, it.name, null, false)
                            }.emptyToNull(), false)
                        } else {
                            it
                        }
                    }
                )
            }

            is NavigationNode -> {
                function.visit(modifier = modifier, IdentifierClassifier.Function)
                val receiverType = function.subject.type().unboxClassTypeAsCompanion().toDataType()
                val lookupReceiverTypes = if (!receiverType.isNullable || function.operator == ".") {
                    listOf(receiverType)
                } else { // operator == "?." && receiverType.isNullable
                    listOf(
                        function.subject.type().copy(isNullable = false).toDataType(),
                        function.subject.type().copy(isNullable = true).toDataType(),
                    )
                }

                val resolutions = lookupReceiverTypes.flatMap {
                    currentScope.findMatchingCallables(
                        currentScope,
                        function.member.name,
                        it,
                        arguments.map { FunctionCallArgumentInfo(it.name, it.type(ResolveTypeModifier(isSkipGenerics = true)).toDataType()) },
                        modifierFilter = modifierFilter!!,
                    )
                }
                    .distinctBy {
                        if (it.type == CallableType.ExtensionFunction) {
                            it.transformedName
                        } else {
                            it.signature
                        }
                    }
                if (resolutions.size > 1) {
                    throw SemanticException(position, "Ambiguous function call for `${function.member.name}`. ${resolutions.size} candidates match.")
                }
                val resolution = resolutions.firstOrNull()
                    ?: throw SemanticException(position, "No matching function `${function.member.name}` found for type ${receiverType.nameWithNullable}")

                functionRefName = resolution.transformedName
                callableType = resolution.type

                /**
                 * Return type must be nullable if:
                 * - Callable returns nullable type
                 * - receiver is nullable and operator is "?."
                 */

                val returnType = resolution.returnType.let {
                    var r = if (receiverType.isNullable && function.operator == "?.") {
                        it.copy(isNullable = true)
                    } else {
                        it
                    }
                    val subjectType = function.subject.type().toDataType()
                    log.v { "functionRefName=$functionRefName; subjectType=${subjectType::class.simpleName} ${subjectType.descriptiveName}" }
                    if (subjectType is ObjectType) {
                        val classTypeParameters = subjectType.clazz.typeParameters
                        log.v {
                            "functionRefName=$functionRefName; subjectType=${subjectType.descriptiveName}; subjectType.clazz.typeParameters=${
                                subjectType.clazz.typeParameters.joinToString(
                                    ","
                                ) { it.name }
                            }; subjectType.arguments=${subjectType.arguments.joinToString(",") { it.descriptiveName }}; before r=${r.descriptiveName()}"
                        }
                        val namedTypeArguments = classTypeParameters.mapIndexed { index, it ->
                            it.name to subjectType.arguments[index].toTypeNode()
                        }.toMap()
                        if (namedTypeArguments.isNotEmpty()) {
                            extraTypeResolutions = namedTypeArguments
                            r = r.resolveGenericParameterTypeArguments(namedTypeArguments)
                        }
                        log.v { "functionRefName=$functionRefName; r=${r.descriptiveName()}" }
                    }
                    r
                }

                FunctionInfo(
                    valueParameters = resolution.arguments,
                    typeParameters = resolution.typeParameters,
                    receiverType = resolution.receiverType,
                    returnType = returnType
                )
            }

            else -> { // including `{ ... }()`, `f!!()`, etc.
                function.visit(modifier = modifier)
                val type = function.type()
                if (type is FunctionTypeNode) { // function's return type is FunctionTypeNode
                    if (!type.isNullable) {
                        FunctionInfo(
                            valueParameters = type.parameterTypes!!,
                            typeParameters = emptyList(),
                            receiverType = null,
                            returnType = type.returnType!!
                        )
                    } else {
                        throw SemanticException(position, "${type.descriptiveName()} is not callable")
                    }
                } else {
                    throw SemanticException(position, "${type.descriptiveName()} is not callable")
                }
            }
        }

        // Validate call arguments against declared arguments
        // Check for missing mandatory arguments, extra arguments, duplicated arguments and mismatch data types
//        if (arguments.size > functionArgumentDeclarations.size) {
//            throw SemanticException("Too much arguments. At most ${functionArgumentDeclarations.size} are accepted.")
//        }

        val isVararg = (functionArgumentAndReturnTypeDeclarations.valueParameters.firstOrNull() as? FunctionValueParameterNode)?.let {
            it.modifiers.contains(FunctionValueParameterModifier.vararg)
        } ?: false

        if (declaredTypeArguments.isNotEmpty() && functionArgumentAndReturnTypeDeclarations.typeParameters.size != declaredTypeArguments.size) {
            throw SemanticException(position, "Number of type arguments does not match with number of type parameters of the matched callable")
        }

        declaredTypeArguments.forEach {
            it.visit(modifier = modifier)
        }

        functionArgumentAndReturnTypeDeclarations.typeParameters.forEach {
            currentScope.declareTypeAlias(it.position, it.name, it.typeUpperBound)
        }

        var typeArgumentByName = functionArgumentAndReturnTypeDeclarations.typeParameters
            .let { typeParameters ->
                if (typeArguments.isNotEmpty()) {
                    typeParameters.mapIndexed { index, tp ->
                        tp.name to typeArguments[index]
                    }.toMap()
                } else {
                    emptyMap()
                }
            }

        class ArgumentInfo(val type: DataType, val isOptional: Boolean, val name: String?)
        fun evaluateArguments() = functionArgumentAndReturnTypeDeclarations.valueParameters.map {
            when (it) {
                is DataType -> ArgumentInfo(it, false, null)
                is TypeNode -> ArgumentInfo(it.toDataType(), false, null)
                is FunctionValueParameterNode -> ArgumentInfo(
                    type = if (typeArgumentByName.isEmpty()) {
                        it.type.resolveGenericParameterTypeToUpperBound(
                            functionArgumentAndReturnTypeDeclarations.typeParameters +
                                extraTypeResolutions.map { TypeParameterNode(it.value.position, it.key, it.value) }
                        ) // note the order
                            .toDataType()
                    } else {
                        it.type.resolveGenericParameterTypeArguments(extraTypeResolutions + typeArgumentByName).toDataType() // note the order
                    },
                    isOptional = it.defaultValue != null,
                    name = it.name
                )
                else -> throw UnsupportedOperationException("Unknown internal class ${it::class.simpleName}")
            }
        }
        var argumentInfos = evaluateArguments()
//        val mandatoryArgumentIndexes = argumentInfos.indices.filter { !argumentInfos[it].isOptional }
//
        // callArgumentMappedIndexes[index of `arguments`] = index of matched argument
        val callArgumentMappedIndexes = arguments.mapIndexed { i, a ->
            if (isVararg) {
                0
            } else if (a.name == null) {
                if (i == arguments.lastIndex && a.type(ResolveTypeModifier(isSkipGenerics = true)) is FunctionTypeNode) {
                    argumentInfos.lastIndex
                } else {
                    i
                }
            } else {
                argumentInfos.indexOfFirst { it.name == a.name }.also {
                    if (it < 0) throw SemanticException(a.position, "Argument ${a.name} not found")
                }
            }
        }
//        if (callArgumentMappedIndexes.distinct().size < callArgumentMappedIndexes.size) {
//            throw SemanticException("There are duplicated arguments")
//        }
//        val missingIndexes = mandatoryArgumentIndexes.filter { i -> callArgumentMappedIndexes.none { it == i } }
//        if (missingIndexes.isNotEmpty()) {
//            throw SemanticException("Missing mandatory arguments for index $missingIndexes")
//        }

        val typeParameters = functionArgumentAndReturnTypeDeclarations.typeParameters
        val tpUpperBounds = typeParameters.associate { it.name to (it.typeUpperBound?.toDataType() ?: AnyType(isNullable = true)) }
        var tpResolutions = mutableMapOf<String, TypeNode>()
        fun inferTypeArgumentsFromOtherArguments(isSkipGenerics: Boolean) {
            if (declaredTypeArguments.isEmpty() && functionArgumentAndReturnTypeDeclarations.typeParameters.isNotEmpty()) {
                // infer type arguments from value arguments

                fun inferNestedTypeArgument(resolvedArgumentTypeName: String) {
                    if (tpUpperBounds[resolvedArgumentTypeName] !is ObjectType) return

                    var t: DataType? = currentScope.assertToDataType(tpResolutions[resolvedArgumentTypeName]!!)
                    while (t is ObjectType && t.name != tpUpperBounds[resolvedArgumentTypeName]!!.name) {
                        t = t.superType
                    }
                    (tpUpperBounds[resolvedArgumentTypeName] as? ObjectType)?.arguments?.forEachIndexed { i, it ->
                        if (tpUpperBounds.containsKey(it.name)) {
                            val resolvedArgumentType = (t as ObjectType).arguments[i].toTypeNode()
                            tpResolutions[it.name] = superTypeOf(tpResolutions[it.name] ?: resolvedArgumentType, resolvedArgumentType)
                        }
                    }
                }

                fun inferTypeArgumentFromOtherArgument(parameterType: TypeNode, argumentType: TypeNode) {
                    if (tpUpperBounds.containsKey(parameterType.name)) {
                        tpResolutions[parameterType.name] =
                            superTypeOf(tpResolutions.getOrElse(parameterType.name) { argumentType }, argumentType)
                        inferNestedTypeArgument(parameterType.name)
                    } /*else if (parameterType.arguments?.any { tpUpperBounds.containsKey(it.name) } == true) {
                        parameterType.arguments.withIndex().filter { tpUpperBounds.containsKey(it.value.name) }
                            .forEach {
                                val tp = it.value.name
                                val argType = (argumentType.arguments ?: return@forEach)[it.index]
                                tpResolutions[tp] = superTypeOf(tpResolutions.getOrElse(tp) { argType }, argType)
                                inferNestedTypeArgument(tp)
                            }
                    }*/
                    parameterType.arguments?.withIndex()
                        ?.forEach {
                            inferTypeArgumentFromOtherArgument(it.value, (argumentType.arguments ?: return@forEach)[it.index])
                        }
                }

                arguments.forEachIndexed { i, callArg ->
                    val parameterType =
                        functionArgumentAndReturnTypeDeclarations.valueParameters[callArgumentMappedIndexes[i]].let { vp ->
                            when (vp) {
                                is DataType -> vp.toTypeNode()
                                is TypeNode -> vp
                                is FunctionValueParameterNode -> vp.type
                                else -> throw UnsupportedOperationException("Unknown internal class ${vp::class.simpleName}")
                            }
                        }
                    val argumentType = callArg.type(ResolveTypeModifier(isSkipGenerics = isSkipGenerics))
                    inferTypeArgumentFromOtherArgument(parameterType = parameterType, argumentType = argumentType)
                }
                if (functionArgumentAndReturnTypeDeclarations.receiverType != null) {
                    val parameterType = functionArgumentAndReturnTypeDeclarations.receiverType
                    val argumentType =
                        (function as NavigationNode).subject.type(ResolveTypeModifier(isSkipGenerics = isSkipGenerics))

                    var type = argumentType.toDataType() as? ObjectType
                    val resolver = type?.let { ClassMemberResolver(it.clazz, it.arguments.map { it.toTypeNode() }) }
                    while (type != null && type.name != parameterType.name) {
//                        type = (type as? ObjectType)?.clazz?.superClass?.let {
//                            val typeResolutions = resolver!!.genericResolutions[it.index].second
//                            ObjectType(it, it.typeParameters.map {
//                                typeResolutions[it.name]!!.toDataType()
//                            }, superType = null)
//                        }
                        type = type.superType
                    }

                    if (type != null) {
                        inferTypeArgumentFromOtherArgument(parameterType = parameterType, argumentType = type.toTypeNode())
                    }
                }
                // check at this point would miss generic lambda resolution
//            if (tpResolutions.size != tpUpperBounds.size) {
//                val missing = tpUpperBounds.map { it.key }.toSet() - tpResolutions.map { it.key }.toSet()
//                throw CannotInferTypeException("type: ${missing.joinToString(", ")}")
//            }
//            if (tpUpperBounds.any {
//                    !it.value.isAssignableFrom(tpResolutions[it.key]!!.toDataType())
//                }) throw SemanticException("Given value arguments are out of bound of type parameters")

                inferredTypeArguments = functionArgumentAndReturnTypeDeclarations.typeParameters.map { tp ->
                    tpResolutions[tp.name]
                }
                typeArgumentByName = tpResolutions
            }
        }
        inferTypeArgumentsFromOtherArguments(isSkipGenerics = true)
        argumentInfos = evaluateArguments() // update upper bounds of generic lambda

//        if (typeArguments.size != functionArgumentAndReturnTypeDeclarations.typeParameters.size) {
//            throw SemanticException("Number of type arguments does not match with number of type parameters of the matched callable")
//        }

        arguments.forEachIndexed { i, callArgument ->
            val functionArgumentType = argumentInfos[callArgumentMappedIndexes[i]].type
            if (callArgument.value is LambdaLiteralNode && functionArgumentType is FunctionType) {
                if (callArgument.value.valueParameters.size != functionArgumentType.arguments.size && !(callArgument.value.valueParameters.isEmpty() && functionArgumentType.arguments.size == 1)) {
                    throw SemanticException(callArgument.position, "Lambda argument count is different from function parameter declaration.")
                }
                callArgument.value.parameterTypesUpperBound = functionArgumentType.arguments.map {
                    it.toTypeNode()
                }
                callArgument.value.returnTypeUpperBound = functionArgumentType.returnType.toTypeNode()
            }
        }

        // revisit to resolve generic lambda type parameters
        // visit argument must before evaluating type
        arguments.forEach { it.visit(modifier = modifier) }

        // use resolved type parameters in generic lambda arguments to resolve function type parameters
        inferTypeArgumentsFromOtherArguments(isSkipGenerics = false)
        argumentInfos = evaluateArguments()

        // check for mismatch generic parameters
        arguments.forEachIndexed { i, callArgument ->
            val functionArgumentType = argumentInfos[callArgumentMappedIndexes[i]].type
            if (!functionArgumentType.isConvertibleFrom(callArgument.type().toDataType())) {
                throw SemanticException(callArgument.position, "Call argument's type ${callArgument.type().descriptiveName()} cannot be mapped to type ${argumentInfos[callArgumentMappedIndexes[i]].type.descriptiveName}")
            }
        }

        if (typeArguments.size != typeParameters.size) {
            val missing = (typeParameters.indices - (inferredTypeArguments?.withIndex()?.filter { it.value != null }?.map { it.index }?.toSet() ?: emptySet())).map { typeParameters[it].name }
            throw CannotInferTypeException(position, "type: ${missing.joinToString(", ")}")
        }
        tpResolutions = typeArguments.mapIndexed { index, t ->
            typeParameters[index].name to t
        }.toMap().toMutableMap()
        tpUpperBounds.forEach {
            if (!it.value.isConvertibleFrom(tpResolutions[it.key]!!.toDataType())) {
                throw SemanticException(tpResolutions[it.key]!!.position, "Provided value arguments are out of bound of type parameters")
            }
        }

        returnType = functionArgumentAndReturnTypeDeclarations.returnType!!.let { returnType ->
            if (callableType == CallableType.Constructor &&
                declaredTypeArguments.isEmpty() &&
                typeArgumentByName.any { it.key == returnType.name && returnType.arguments?.any { a -> a.name == it.key } == true }
                ) {
                /** not supported until {@link TypeInferenceTest#functionWithClassNameIsSameAsTypeParameterNameNested} is fixed */
                throw CannotInferTypeException(position, "type: " + typeArgumentByName.filter { it.key == returnType.name && returnType.arguments?.any { a -> a.name == it.key } == true }.keys.first())
            }

            if (callableType != CallableType.Constructor || declaredTypeArguments.isEmpty()) {
                returnType.resolveGenericParameterTypeArguments(typeArgumentByName)
            } else {
                /**
                 * not resolving generic parameters in order to support this case:
                 * ```
                 *   class T<T>
                 *   val o: T<T<T<Int>>> = T<T<T<Int>>>()
                 * ```
                 */
                TypeNode(returnType.position, returnType.name, typeArguments.emptyToNull(), false)
            }
        }

        popScope()

        // record types if there is any enclosing lambda
        evaluateAndRegisterReturnType(this)
    }

    fun FunctionValueParameterNode.visit(modifier: Modifier = Modifier(), functionDeclarationNode: FunctionDeclarationNode?) {
        if (defaultValue is LambdaLiteralNode && type is FunctionTypeNode) {
            defaultValue.parameterTypesUpperBound = (type as FunctionTypeNode).parameterTypes
        }
        defaultValue?.visit(modifier = modifier)
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException(position, "Property `$name` has already been declared")
        }
        if (defaultValue != null) {
            val valueType = defaultValue.type().toDataType()
            val subjectType = type.toDataType()
            if (!subjectType.isAssignableFrom(valueType)) {
                throw TypeMismatchException(position, subjectType.nameWithNullable, valueType.nameWithNullable)
            }
        }
        if (declaredType == null && inferredType == null) {
            throw SemanticException(position, "Cannot infer lambda parameter type")
        }
        val type = functionDeclarationNode?.resolveGenericParameterType(this) ?: type
        currentScope.declareProperty(position = position, name = name, type = type, isMutable = false)
        currentScope.assign(name, SemanticDummyRuntimeValue(currentScope.typeNodeToPropertyType(type, false)!!.type))
        transformedRefName = "$name/${currentScope.scopeLevel}"
        currentScope.registerTransformedSymbol(position, IdentifierClassifier.Property, transformedRefName!!, name)
    }

    fun FunctionCallArgumentNode.visit(modifier: Modifier = Modifier()) {
        value.visit(modifier = modifier)
    }

    fun BlockNode.visit(modifier: Modifier = Modifier()) {
        pushScope(
            scopeName = "<block>",
            scopeType = if (type == ScopeType.Function) ScopeType.FunctionBlock else type,
        )

        statements.forEachIndexed { i, it ->
            if (returnTypeUpperBound is FunctionTypeNode && i == statements.lastIndex && it is LambdaLiteralNode) {
                val returnTypeUpperBound = returnTypeUpperBound as FunctionTypeNode
                it.parameterTypesUpperBound = returnTypeUpperBound.parameterTypes
                it.returnTypeUpperBound = returnTypeUpperBound.returnType
            }
            it.visit(modifier = modifier)
        }

        returnType = type()

        popScope()
    }

    fun ReturnNode.visit(modifier: Modifier = Modifier()) {
        var s: SymbolTable = currentScope
        while (s.scopeType != ScopeType.Function) {
            if (s.scopeType == ScopeType.Script || s.parentScope == null) {
                throw SemanticException(position, "`return` statement should be within a function")
            }
            if (s.scopeType == ScopeType.Closure) {
                if (returnToLabel.isEmpty()) {
                    throw SemanticException(position, "Direct `return` statement cannot live inside a lambda")
                } else {
                    if (s.scopeName != "$returnToLabel@") {
                        throw SemanticException(position, "Return label `$returnToLabel` does not match the enclosing lambda label")
                    }
                    break
                }
            }
            s = s.parentScope!!
        }
        if (returnToLabel.isNotEmpty() && s.scopeType != ScopeType.Closure) {
            throw SemanticException(position, "Return label `$returnToLabel` not found")
        }
        // TODO block return in lambda
        // s.scopeType == ScopeType.Function
        val declaredReturnType = s.returnType
        if (declaredReturnType is FunctionType && value is LambdaLiteralNode) {
            value.parameterTypesUpperBound = declaredReturnType.arguments.map { it.toTypeNode() }
            value.returnTypeUpperBound = declaredReturnType.returnType.toTypeNode()
        }

        value?.visit(modifier = modifier)
        val valueType = value?.type()?.toDataType() ?: UnitType()
        if (declaredReturnType != null && !declaredReturnType.isAssignableFrom(valueType)) {
            throw TypeMismatchException(position, s.returnType!!.nameWithNullable, valueType.nameWithNullable)
        }
    }

    fun checkBreakOrContinueScope(statement: ASTNode) {
        var s: SymbolTable = currentScope
        while (!s.scopeType.isLoop()) {
            if (s.scopeType in setOf(ScopeType.Script, ScopeType.Function) || s.parentScope == null) {
                throw SemanticException(statement.position, "`break` statement should be within a loop")
            }
            s = s.parentScope!!
        }
    }

    fun BreakNode.visit(modifier: Modifier = Modifier()) {
        checkBreakOrContinueScope(this)
    }

    fun ContinueNode.visit(modifier: Modifier = Modifier()) {
        checkBreakOrContinueScope(this)
    }

    fun IfNode.visit(modifier: Modifier = Modifier()) {
        condition.visit(modifier = modifier)
        trueBlock?.visit(modifier = modifier)
        falseBlock?.visit(modifier = modifier)
    }

    fun WhileNode.visit(modifier: Modifier = Modifier()) {
        condition.visit(modifier = modifier)
        body?.visit(modifier = modifier)
    }

    fun NavigationNode.visit(modifier: Modifier = Modifier(), lookupType: IdentifierClassifier = IdentifierClassifier.Property, isCheckWriteAccess: Boolean = false) {
        subject.visit(modifier = modifier)

        // at this moment subject must not be a primitive
        member.visit(modifier = modifier)

        // find member
        val subjectType = subject.type().unboxClassTypeAsCompanion().toDataType()
        val subjectTypesToSearch = if (operator == "?." && subjectType.isNullable) {
            listOf(subjectType, subjectType.copyOf(isNullable = false))
        } else {
            listOf(subjectType)
        }

        val memberName = member.name

        for (subjectType in subjectTypesToSearch) {
            val clazz = subjectType.resolveTypeParameterAsUpperBound().nameWithNullable
                .let {
                    currentScope.findClass(it)
                        ?: throw RuntimeException("Cannot find class `$it`")
                }
                .first
            if (lookupType == IdentifierClassifier.Property) {
                clazz.findMemberPropertyWithoutAccessor(memberName)?.let { property ->
                    if (isCheckWriteAccess && !property.isMutable) {
                        throw SemanticException(position, "val `$memberName` cannot be reassigned")
                    }
                    return
                }
                clazz.findMemberPropertyCustomAccessor(memberName)?.let { accessor ->
                    if (isCheckWriteAccess) {
                        if (accessor.setter == null) {
                            throw SemanticException(position, "Setter for `$memberName` is not declared")
                        }
                    } else if (accessor.getter == null) {
                        throw SemanticException(position, "Getter for `$memberName` is not declared")
                    }
                    return
                }
                val resolvedSubjectType = subjectType.toTypeNode()
//                .let {
//                    if (subject is ClassInstance) {
//                        it.resolveGenericParameterTypeArguments(subject.typeArgumentByName.mapValues { it.value.toTypeNode() })
//                    } else {
//                        it
//                    }
//                }
                currentScope.findExtensionPropertyByDeclarationIncludingSuperClasses(resolvedSubjectType, memberName)
                    ?.let {
                        if (isCheckWriteAccess && it.second.setter == null) {
                            throw SemanticException(position, "Setter for `$memberName` is not declared")
                        } else if (!isCheckWriteAccess && it.second.getter == null) {
                            throw SemanticException(position, "Getter for `$memberName` is not declared")
                        }
                        transformedRefName = it.first
                        return
                    }
            } else {
                if (clazz.findMemberFunctionsByDeclaredName(memberName).isNotEmpty()) return
                if (currentScope.findExtensionFunctionsIncludingSuperClasses(subjectType, memberName)
                        .isNotEmpty()
                ) return
            }
        }
        throw SemanticException(position, "Type `${subjectType.descriptiveName}` has no member `$memberName`")
    }

    fun IndexOpNode.visit(modifier: Modifier = Modifier(), isWriteOnly: Boolean = false) {
        if (hasFunctionCall != null) {
            return
        }
        if (isWriteOnly) {
            subject.visit(modifier)
            arguments.forEach {
                visit(modifier)
                type()
            }
            hasFunctionCall = false
        } else {
            hasFunctionCall = true
            call = FunctionCallNode(
                function = NavigationNode(position, subject, ".", ClassMemberReferenceNode(position, "get")),
                arguments = arguments.mapIndexed { index, it -> FunctionCallArgumentNode(it.position, index = index, value = it) },
                declaredTypeArguments = emptyList(),
                position = position,
                modifierFilter = SearchFunctionModifier.OperatorFunctionOnly,
            )
            call!!.visit(modifier)
        }
    }

    fun ClassDeclarationNode.visit(modifier: Modifier = Modifier()) {
        val fullQualifiedClassName = fullQualifiedName
        val classType = TypeNode(
            position = position,
            name = fullQualifiedClassName,
            arguments = typeParameters.map { TypeNode(it.position, it.name, null, false) }.emptyToNull(),
            isNullable = false,
        )

        val declarationScope = currentScope

        // Declare nullable class type
        declarationScope.declareClass(
            position,
            ClassDefinition(
                currentScope = currentScope,
                name = "$name?",
                fullQualifiedName = "$fullQualifiedClassName?",
                modifiers = emptySet(),
                typeParameters = typeParameters,
                isInstanceCreationAllowed = false,
                orderedInitializersAndPropertyDeclarations = emptyList(),
                declarations = emptyList(),
                rawMemberProperties = emptyList(),
                memberFunctions = emptyMap(),
                primaryConstructor = null,
            )
        )

        // Declare companion object
        declarationScope.declareClass(
            position,
            ClassDefinition(
                currentScope = currentScope,
                name = "$name.Companion",
                fullQualifiedName = "$fullQualifiedClassName.Companion",
                modifiers = emptySet(),
                typeParameters = emptyList(),
                isInstanceCreationAllowed = false,
                orderedInitializersAndPropertyDeclarations = emptyList(),
                declarations = emptyList(),
                rawMemberProperties = emptyList(),
                memberFunctions = emptyMap(),
                primaryConstructor = null,
            )
        )

        pushScope(name, ScopeType.Class)
        // copy this class's type parameter to superclass scope, so that
        // generic types in superclass can be resolved when it returns to this class.
        // declared type parameters here will be overwritten in this class's scope
        typeParameters.forEach {
            currentScope.declareTypeAlias(it.position, it.name, it.typeUpperBound)
        }

        pushScope(name, ScopeType.Class)
        val superClassScope = currentScope
        val superClass = (superClassInvocation?.function as? TypeNode)
            ?.let { declarationScope.findClass(it.name) ?: throw RuntimeException("Super class `${it.name}` not found") }
            ?.first
        if (superClass != null && ClassModifier.open !in superClass.modifiers) {
            throw SemanticException(superClassInvocation!!.position, "A class can only extend from an open class")
        }
        val superClassFunctions = superClass?.getAllMemberFunctions()
        val superClassProperties = superClass?.getAllMemberProperties()

        fun checkForOverriddenProperties(property: PropertyDeclarationNode) {
            if (superClassProperties?.containsKey(property.name) == true) {
                if (PropertyModifier.override !in property.modifiers) {
                    throw SemanticException(property.position, "A property cannot override anything without the `override` modifier")
                }
                if (superClass.findDeclarations { _, it ->
                    it is PropertyDeclarationNode && it.name == property.name
                }.any { PropertyModifier.open !in (it as PropertyDeclarationNode).modifiers }) {
                    throw SemanticException(property.position, "A property can only override another property marked as `open`")
                }
            } else {
                if (PropertyModifier.override in property.modifiers) {
                    throw SemanticException(property.position, "Property `${property.name}` overrides nothing")
                }
            }
        }

        pushScope(name, ScopeType.Class)
        run {
            typeParameters.forEach {
                currentScope.declareTypeAlias(it.position, it.name, it.typeUpperBound)
            }
            primaryConstructor?.visit(modifier = modifier)
            superClassInvocation?.visit(modifier = modifier)
            if (superClassInvocation != null) {
                temporarilySwitchToScopeAndRun(superClassScope) {
                    val type = superClassInvocation.type()
                    val typeArguments = superClass!!.typeParameters.mapIndexed { index, tp ->
                        tp.name to type.arguments!![index]
                    }.toMap()
                    superClass.currentScope?.let { currentScope.mergeDeclarationsFrom(position, it, typeArguments) }
                }
            }

            val nonPropertyArguments = primaryConstructor?.parameters
                ?.filter { !it.isProperty }
            primaryConstructor?.parameters?.forEach {
                currentScope.undeclareProperty(it.parameter.name)
                currentScope.unregisterTransformedSymbol(IdentifierClassifier.Property, it.parameter.transformedRefName!!)
            }
            primaryConstructor?.parameters
                ?.filter { it.isProperty }
                ?.forEach {
                    val p = it.parameter
                    currentScope.declareProperty(position = p.position, name = p.name, type = p.type, isMutable = it.isMutable)
                    currentScope.assign(p.name, SemanticDummyRuntimeValue(currentScope.getPropertyType(p.name).first.type))
                }

            val classDefinition = ClassDefinition(
                currentScope = currentScope!!,
                name = name,
                fullQualifiedName = fullQualifiedName,
                modifiers = modifiers,
                typeParameters = typeParameters,
                isInstanceCreationAllowed = true,
                primaryConstructor = primaryConstructor,
                rawMemberProperties = primaryConstructor?.parameters
                    ?.filter { it.isProperty }
                    ?.map {
                        val p = it.parameter
                        PropertyDeclarationNode(
                            position = p.position,
                            name = p.name,
                            declaredModifiers = it.modifiers,
                            typeParameters = emptyList(),
                            receiver = classType,
                            declaredType = p.type,
                            isMutable = it.isMutable,
                            initialValue = p.defaultValue,
                            transformedRefName = p.transformedRefName,
                        ).also { checkForOverriddenProperties(it) }
                    } ?: emptyList() /* intentionally exclude non-constructor property declarations, in order to allow inferring types */,
                memberFunctions = declarations
                    .filterIsInstance<FunctionDeclarationNode>()
                    .associateBy {
                        it.toSignature(currentScope as SemanticAnalyzerSymbolTable).also { signature ->
                            log.v { "Class `$name` member function `${it.name}` signature = `$signature`" }
                            val superClassFunction = superClassFunctions?.get(signature)
                            if (superClassFunction != null) {
                                if (FunctionModifier.open !in superClassFunction.modifiers) {
                                    throw SemanticException(it.position, "A function cannot override another function not marked as `open`")
                                }
                                if (FunctionModifier.override !in it.modifiers) {
                                    throw SemanticException(it.position, "A function cannot override anything without the modifier `override`")
                                }
                            } else {
                                if (FunctionModifier.override in it.modifiers) {
                                    throw SemanticException(it.position, "Function `${it.name}` overrides nothing")
                                }
                            }
                        }
                    },
                orderedInitializersAndPropertyDeclarations = declarations
                    .filter { it is ClassInstanceInitializerNode || it is PropertyDeclarationNode },
                declarations = declarations,
                superClassInvocation = superClassInvocation,
                superClass = superClass,
            )

            declarationScope.declareClass(position, classDefinition)

            // typeParameters.map { it.typeUpperBound ?: TypeNode("Any", null, true) }.emptyToNull()
            val pseudoTypeArguments = typeParameters.map { TypeNode(it.position, it.name, null, false) }.emptyToNull()
            currentScope.declareProperty(position, "this", TypeNode(SourcePosition.NONE, name, pseudoTypeArguments, false), false)
            currentScope.registerTransformedSymbol(position, IdentifierClassifier.Property, "this", "this")
            currentScope.declareProperty(position, "this/${fullQualifiedClassName}", TypeNode(SourcePosition.NONE, name, pseudoTypeArguments, false), false)
            currentScope.registerTransformedSymbol(position, IdentifierClassifier.Property, "this/${fullQualifiedClassName}", "this")

            superClassInvocation?.let { superClassInvocation ->
                currentScope.declareProperty(position, "super", superClassInvocation.type(), false)
                currentScope.registerTransformedSymbol(position, IdentifierClassifier.Property, "super", "super")
            }

            primaryConstructor?.parameters
                ?.filter { it.isProperty }
                ?.map { it.parameter }
                ?.forEach { currentScope.declarePropertyOwner(it.transformedRefName!!, "this/$fullQualifiedClassName") }
            declarations.filter { it is ClassInstanceInitializerNode || it is PropertyDeclarationNode }
                .forEach {
                    pushScope("init-property", ScopeType.ClassInitializer)
                    nonPropertyArguments?.forEach {
                        val parameterForClassBody = it.parameter.copy()
                        parameterForClassBody.visit(modifier = modifier, null /* TODO support generic class */)
                        it.transformedRefNameInBody = parameterForClassBody.transformedRefName
                    }
                    pushScope("init-property-inner", ScopeType.ClassInitializer)
                    if (it is PropertyDeclarationNode) {
                        it.visit(modifier = modifier, isClassProperty = true)
                    } else {
                        it.visit(modifier = modifier)
                    }
                    popScope()
                    popScope()
                    if (it is PropertyDeclarationNode) {
                        it.visit(modifier = modifier, isVisitInitialValue = false, isClassProperty = true)
                        checkForOverriddenProperties(it)
                        currentScope.declarePropertyOwner(it.transformedRefName!!, "this/$fullQualifiedClassName")
                        classDefinition.addProperty(currentScope, it)
                    }
                }

            declarations.filterIsInstance<FunctionDeclarationNode>()
                .forEach {
//                    it.transformedRefName = "${it.name}/${++functionDefIndex}"
                    it.transformedRefName = it.toSignature(currentScope)
                    currentScope.declareFunctionOwner(it.name, it, "this/$fullQualifiedClassName")
                }

            declarations.filterIsInstance<FunctionDeclarationNode>()
                .forEach {
                    it.visit(modifier = modifier, isClassMemberFunction = true)
                }
        }
        popScope()
        popScope()
        popScope()

        if (currentScope !== declarationScope) {
            throw RuntimeException("Original scope is not restored")
        }
    }

    fun ClassPrimaryConstructorNode.visit(modifier: Modifier = Modifier()) {
        parameters.forEach { it.visit(modifier = modifier) }
    }

    fun ClassParameterNode.visit(modifier: Modifier = Modifier()) {
        parameter.visit(modifier = modifier, null /* TODO generic class */)
    }

    fun ClassInstanceInitializerNode.visit(modifier: Modifier = Modifier()) {
        block.visit(modifier = modifier)
    }

    fun ClassMemberReferenceNode.visit(modifier: Modifier = Modifier()) {
        // TODO check for write access
        // check for existence and return error
        try {
            val l = checkPropertyReadAccess(this, name)
            if (transformedRefName == null) {
                transformedRefName = "$name/$l"
            }
        } catch (_: SemanticException) {}
    }

    fun StringNode.visit(modifier: Modifier = Modifier()) {
        nodes.forEach { it.visit(modifier = modifier) }
    }

    fun LambdaLiteralNode.visit(modifier: Modifier = Modifier()) {
//        val type = type() as FunctionTypeNode

        if (modifier.isSkipGenerics) return

        symbolRecorders += SymbolReferenceSet(scopeLevel = currentScope.scopeLevel)
        pushScope(
            scopeName = label?.label?.let { "$it@" } ?: "<lambda>",
            scopeType = ScopeType.Closure,
            returnType = returnTypeUpperBound?.toDataType() //type.returnType.toDataType(),
        )

        if (parameterTypesUpperBound != null) {
            if (valueParameters.size != parameterTypesUpperBound!!.size) {
                throw SemanticException(position, "Lambda argument count is different from function parameter declaration.")
            }
            valueParameters.forEachIndexed { i, lambdaParameterNode ->
                if (lambdaParameterNode.declaredType == null) {
                    lambdaParameterNode.inferredType = parameterTypesUpperBound!![i]
                }
            }
        }

        valueParameters.forEach {
            if (it.name != "_") {
                it.visit(modifier = modifier, null)
            }
        }
        // TODO provide receiver to scope if exists

        body.returnTypeUpperBound = returnTypeUpperBound
        body.visit(modifier = modifier)

        if (returnTypeUpperBound?.toDataType()?.isConvertibleFrom(body.type().toDataType()) == false) {
            throw SemanticException(position, "Lambda return type ${body.type().descriptiveName()} cannot be converted to ${returnTypeUpperBound!!.descriptiveName()}")
        }

        popScope()
        this.accessedRefs = symbolRecorders.removeLast()
        if (symbolRecorders.isNotEmpty()) {
            val symbols = symbolRecorders.last()
            symbols.properties += this.accessedRefs!!.properties
                .filter {
                    currentScope.findTransformedSymbol(IdentifierClassifier.Property, it)?.second?.scopeLevel?.let {
                        isLocalAndNotCurrentScope(it)
                    } ?: false
                }
            symbols.functions += this.accessedRefs!!.functions
                .filter {
                    currentScope.findTransformedSymbol(IdentifierClassifier.Function, it)?.second?.scopeLevel?.let {
                        isLocalAndNotCurrentScope(it)
                    } ?: false
                }
            symbols.classes += this.accessedRefs!!.classes
            symbols.typeAlias += this.accessedRefs!!.typeAlias
        }
        type()
    }

    fun AsOpNode.visit(modifier: Modifier = Modifier()) {
        this.expression.visit(modifier = modifier)
        this.type.visit(modifier = modifier)
    }

    fun InfixFunctionCallNode.visit(modifier: Modifier = Modifier()) {
        if (functionName !in setOf("to", "is", "!is")) {
            throw SemanticException(position, "Infix function `$functionName` is not supported")
        }
        this.node1.visit(modifier = modifier)
        this.node2.visit(modifier = modifier)

        type()
    }

    fun ElvisOpNode.visit(modifier: Modifier = Modifier()) {
        this.primaryNode.visit(modifier = modifier)
        this.fallbackNode.visit(modifier = modifier)

        type()
    }

    fun ThrowNode.visit(modifier: Modifier = Modifier()) {
        this.value.visit(modifier = modifier)
        val type = this.value.type()
        if (!typeRegistry["Throwable"]!!.toDataType().isAssignableFrom(type.toDataType())) {
            throw SemanticException(value.position, "Expression type ${type.descriptiveName()} is not a throwable value")
        }
    }

    fun CatchNode.visit(modifier: Modifier = Modifier()) {
        if (!typeRegistry["Throwable"]!!.toDataType().isAssignableFrom(catchType.toDataType())) {
            throw SemanticException(catchType.position, "${catchType.descriptiveName()} is not a throwable value")
        }

        pushScope(
            scopeName = "<catch>",
            scopeType = ScopeType.ExtraWrap,
            returnType = null,
        )

        if (valueName != "_") {
            currentScope.declareProperty(position = position, name = valueName, type = catchType, isMutable = false)
            currentScope.assign(
                name = valueName,
                value = SemanticDummyRuntimeValue(currentScope.typeNodeToPropertyType(catchType, false)!!.type)
            )
            valueTransformedRefName = "$valueName/${currentScope.scopeLevel}"
            currentScope.registerTransformedSymbol(
                position = position,
                identifierClassifier = IdentifierClassifier.Property,
                transformedName = valueTransformedRefName!!,
                originalName = valueName
            )
        }

        this.block.visit(modifier = modifier)
        type()

        popScope()
    }

    fun TryNode.visit(modifier: Modifier = Modifier()) {
        mainBlock.visit(modifier = modifier)
        catchBlocks.forEach { it.visit(modifier = modifier) }
        finallyBlock?.visit(modifier = modifier)

        type()
    }

    fun WhenSubjectNode.visit(modifier: Modifier = Modifier()) {
        value.visit(modifier = modifier)
        val valueType = value.type()
        if (declaredType != null && !declaredType.toDataType().isAssignableFrom(valueType.toDataType())) {
            throw TypeMismatchException(declaredType.position, declaredType.descriptiveName(), valueType.descriptiveName())
        }
        type = declaredType ?: valueType

        if (hasValueDeclaration()) {
            currentScope.declareProperty(position = position, name = valueName!!, type = type!!, isMutable = false)
            currentScope.assign(
                name = valueName,
                value = SemanticDummyRuntimeValue(currentScope.typeNodeToPropertyType(type!!, false)!!.type)
            )
            valueTransformedRefName = "$valueName/${currentScope.scopeLevel}"
            currentScope.registerTransformedSymbol(
                position = position,
                identifierClassifier = IdentifierClassifier.Property,
                transformedName = valueTransformedRefName!!,
                originalName = valueName
            )
        }
    }

    fun WhenConditionNode.visit(modifier: Modifier = Modifier(), isWithoutSubject: Boolean) {
        expression.visit(modifier = modifier)
        val expressionType = expression.type()

        if (isWithoutSubject) {
            if (testType != WhenConditionNode.TestType.Regular) {
                throw SemanticException(expression.position, "Only boolean expression is allowed in a `when` expression without subject")
            }
            if (!typeRegistry["Boolean"]!!.toDataType().isAssignableFrom(expressionType.toDataType())) {
                throw SemanticException(expression.position, "Only boolean expression is allowed in a `when` expression without subject")
            }
        }

        if (testType == WhenConditionNode.TestType.TypeTest && expression !is TypeNode) {
            throw SemanticException(expression.position, "`is` must be followed by a type")
        }

        type = expressionType
    }

    fun WhenEntryNode.visit(modifier: Modifier = Modifier(), isWithoutSubject: Boolean) {
        if (isWithoutSubject && conditions.size > 1) {
            throw SemanticException(position, "Use '||' instead of commas in when-condition for 'when' without argument")
        }
        conditions.forEach { it.visit(modifier = modifier, isWithoutSubject = isWithoutSubject) }

        body.visit(modifier = modifier)
        bodyType = body.type()
    }

    fun WhenNode.visit(modifier: Modifier = Modifier()) {
        pushScope(
            scopeName = "<when>",
            scopeType = ScopeType.WhenOuter,
            returnType = null,
        )

        subject?.visit(modifier = modifier)

        val numOfElse = entries.count { it.isElseCondition() }
        if (numOfElse < 1) {
            throw SemanticException(position, "Currently, `when` expression must be used with an `else` branch")
        }
        if (numOfElse > 1) {
            throw SemanticException(position, "`when` expression can only contain one `else` branch")
        }
        val elseIndex = entries.indexOfFirst { it.isElseCondition() }
        if (elseIndex != entries.lastIndex) {
            throw SemanticException(entries[elseIndex].position, "`else` branch must be the last branch of a `when` expression")
        }

        entries.forEach { it.visit(modifier = modifier, isWithoutSubject = subject == null) }

        // there is at least 1 else branch, so there is at least 1 branch
        type = superTypeOf(*entries.map { it.bodyType }.toTypedArray())

        popScope()
    }

    fun analyze() = scriptNode.visit()

    ////////////////////////////////////

    data class ResolveTypeModifier(val isSkipGenerics: Boolean = false)

    fun ASTNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier())
        = when (this) {
            is AsOpNode -> this.type.copy(this.type.isNullable || this.isNullable)
            is AssignmentNode -> typeRegistry["Unit"]!!
            is BinaryOpNode -> this.type(modifier = modifier)
            is UnaryOpNode -> this.type(modifier = modifier)
            is BlockNode -> this.type(modifier = modifier)
            is BreakNode -> typeRegistry["Unit"]!!
            is ClassDeclarationNode -> typeRegistry["Unit"]!!
            is ClassInstanceInitializerNode -> TODO()
            is ClassMemberReferenceNode -> TODO()
            is ClassParameterNode -> TODO()
            is ClassPrimaryConstructorNode -> TODO()
            is ContinueNode -> typeRegistry["Unit"]!!
            is FunctionCallArgumentNode -> this.type(modifier = modifier)
            is FunctionCallNode -> this.type(modifier = modifier)
            is FunctionDeclarationNode -> typeRegistry["Unit"]!!
            is FunctionValueParameterNode -> type
            is IfNode -> this.type(modifier = modifier)
            is IndexOpNode -> this.type(modifier = modifier)
            is NavigationNode -> this.type(modifier = modifier)
            is PropertyAccessorsNode -> this.type
            is PropertyDeclarationNode -> typeRegistry["Unit"]!!
            is ReturnNode -> this.type(modifier = modifier)
            is ScriptNode -> TODO()
            is TypeNode -> this
            is TypeParameterNode -> TODO()
            is ValueNode -> TODO()
            is VariableReferenceNode -> this.type(modifier = modifier)
            is WhileNode -> typeRegistry["Unit"]!!

            is IntegerNode -> typeRegistry["Int"]!!
            is LongNode -> typeRegistry["Long"]!!
            is DoubleNode -> typeRegistry["Double"]!!
            is BooleanNode -> typeRegistry["Boolean"]!!
            NullNode -> typeRegistry["Null"]!!
            is StringLiteralNode -> TODO()
            is StringNode -> typeRegistry["String"]!!
            is LambdaLiteralNode -> this.type(modifier = modifier)
            is CharNode -> typeRegistry["Char"]!!
            is InfixFunctionCallNode -> this.type(modifier = modifier)
            is ElvisOpNode -> this.type(modifier = modifier)
            is ThrowNode -> typeRegistry["Nothing"]!!
            is CatchNode -> this.type(modifier = modifier)
            is TryNode -> this.type(modifier = modifier)
            is WhenConditionNode -> TODO()
            is WhenEntryNode -> TODO()
            is WhenNode -> type!!
            is WhenSubjectNode -> TODO()
            is LabelNode -> TODO()
    }

    fun BinaryOpNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode = type ?: when (operator) {
        "+", "-", "*", "/", "%" -> {
            val t1 = node1.type(modifier = modifier).toDataType()
            val t2 = node2.type(modifier = modifier).toDataType()
            if (t1 is StringType || t1 is NothingType || t2 is StringType || t2 is NothingType) {
                typeRegistry["String"]!!
            } else if ((t1 == DoubleType(isNullable = false) && t2.isNonNullNumberType())
                || (t2 == DoubleType(isNullable = false) && t2.isNonNullNumberType())
            ) {
                typeRegistry["Double"]!!
            } else if ((t1 == LongType(isNullable = false) && t2.isNonNullIntegralType())
                || (t2 == LongType(isNullable = false) && t2.isNonNullIntegralType())
            ) {
                typeRegistry["Long"]!!
            } else if (t1 == IntType(isNullable = false) && t2 == IntType(isNullable = false)) {
                typeRegistry["Int"]!!
            } else if (operator == "+" && t1 is CharType && t2 is IntType) {
                typeRegistry["Char"]!!
            } else if (operator == "-" && t1 is CharType && t2 is CharType) {
                typeRegistry["Int"]!!
            } else {
                throw SemanticException(position, "Types ${t1.nameWithNullable} and ${t2.nameWithNullable} cannot be applied with operator `$operator`")
            }
        }

        "<", "<=", ">", ">=", "==", "!=", "||", "&&" -> typeRegistry["Boolean"]!!

        else -> throw UnsupportedOperationException()
    }.also {
        type = it
    }

    fun UnaryOpNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        type?.let { return it }
        return if (operator == "!!") {
            node!!.type(modifier = modifier).copy(isNullable = false)
        } else {
            node!!.type(modifier = modifier)
        }.also { type = it }
    }

    // e.g. `name()`, where name is a VariableReferenceNode
    fun VariableReferenceNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()) = type ?: (
            currentScope.findClass(variableName)
                ?.let { ClassTypeNode(TypeNode(position, variableName, null, false)) }
                ?: currentScope.findFunctionsByOriginalName(variableName).firstOrNull()?.let { FunctionTypeNode(position = it.first.position, parameterTypes = null, returnType = null, isNullable = false) }
                ?: currentScope.getPropertyType(variableName).first.type.toTypeNode()!!
            ).also { type = it }

    fun IndexOpNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        return call!!.type(modifier = modifier)
    }

    fun NavigationNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier(), lookupType: IdentifierClassifier = IdentifierClassifier.Property): TypeNode {
        type?.let { return it }
        val subjectType = when(val type = subject.type(modifier = modifier)) {
            is FunctionTypeNode -> type.returnType
            is ClassTypeNode -> type.unboxClassTypeAsCompanion()
            else -> type
        } ?: return typeRegistry["Any"]!!
        val clazz = currentScope.findClass(subjectType.toDataType().resolveTypeParameterAsUpperBound().name)?.first
            ?: throw SemanticException(position, "Unknown type `${subjectType.name}`")
        val memberName = member.name

        fun TypeNode.resolveMemberType(): TypeNode {
            if (subjectType.arguments.isNullOrEmpty()) { // within class declaration
                return this
            }
            return this.resolveGenericParameterTypeArguments(clazz.typeParameters.mapIndexed { index, it ->
                it.name to subjectType.arguments!![index]
            }.toMap())
            // TODO find alias by class name
            // TODO resolve nested type parameters
//            val typeParameterType = currentScope.findTypeAlias(this.name) ?: return this
//            val typeParameterIndex = clazz.typeParameters.indexOfFirst { it.name == this.name }
//            if (typeParameterIndex < 0) throw RuntimeException("Cannot find type parameter $name")
//            val subjectArguments = subjectType.arguments ?: return this //throw RuntimeException("Missing type arguments")
//            return subjectArguments[typeParameterIndex]
        }

        val resolver = ClassMemberResolver(clazz, subjectType.arguments)
        if (lookupType == IdentifierClassifier.Property) {
            resolver.findMemberPropertyCustomAccessorWithType(memberName)?.let {
                return it.second.also { type = it }
            }
            resolver.findMemberPropertyWithoutAccessorWithType(memberName)?.let {
                return it.second.also { type = it }
            }
            transformedRefName?.let {
                currentScope.findExtensionProperty(it)
            }?.let {
                return it.typeNode!!.resolveMemberType()
            }
        } else {
            resolver.findMemberFunctionWithTypeByTransformedName(memberName)?.let {
                return FunctionTypeNode(
                    position = it.function.position,
                    parameterTypes = emptyList(),
                    returnType = it.resolvedReturnType,
                    isNullable = false
                ).also { type = it }
            }
            findExtensionFunction(subjectType.toDataType(), memberName)?.let {
                return FunctionTypeNode(
                    position = it.position,
                    parameterTypes = emptyList(),
                    returnType = it.returnType,
                    isNullable = false
                ).resolveMemberType().also { type = it }
            }
        }

        throw SemanticException(position, "Could not find member `$memberName` for type ${clazz.name}")
    }

    fun FunctionCallArgumentNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        return this.value.type(modifier = modifier)
    }

    fun FunctionCallNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        returnType?.let { return it }
        val functionType = when (function) {
            is NavigationNode -> {
                function.type(modifier = modifier, lookupType = IdentifierClassifier.Function)
            }
            else -> function.type(modifier = modifier)
        }.also {
            if (it is ClassTypeNode) {
                return it.clazz // return directly out to the enclosed function
            }
        }
        if (functionType !is FunctionTypeNode) {
            throw SemanticException(position, "Cannot invoke non-function expression")
        }
        return functionType.returnType!!
    }

    fun ReturnNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        return value?.type(modifier = modifier) ?: typeRegistry["Unit"]!!
    }

    fun IfNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        type?.let { return it }
        return superTypeOf(trueBlock?.type(modifier = modifier), falseBlock?.type(modifier = modifier))
            .also { type = it }
    }

    fun BlockNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        returnType?.let { return it }
        return (statements.lastOrNull()?.type(modifier = modifier) ?: typeRegistry["Unit"]!!)
            .also { returnType = it }
    }

    fun LambdaLiteralNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        if (modifier.isSkipGenerics) return FunctionTypeNode(position = position, parameterTypes = null, returnType = null, isNullable = false)
        type?.let { return it }
        return FunctionTypeNode(position = position, parameterTypes = valueParameters.map { it.type(modifier = modifier) }, returnType = body.type(), isNullable = false)
            .also { type = it }
    }

    fun InfixFunctionCallNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        type?.let { return it }
        return when (functionName) {
            "to" -> TypeNode(SourcePosition.NONE, "Pair", arguments = listOf(node1.type(modifier), node2.type(modifier)), isNullable = false)
            "is", "!is" -> typeRegistry["Boolean"]!!
            else -> throw UnsupportedOperationException("Infix function `$functionName` is not supported")
        }.also { type = it }
    }

    fun ElvisOpNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        type?.let { return it }
        val type1 = primaryNode.type(modifier = modifier).copy(isNullable = false)
        val type2 = fallbackNode.type(modifier = modifier)
        return if (type1.name == "Nothing") {
            type2
        } else {
            superTypeOf(type1, type2)
        }.also { type = it }
    }

    fun CatchNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        type?.let { return it }
        type = block.type(modifier = modifier)
        return type!!
    }

    fun TryNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode {
        type?.let { return it }
        var type = mainBlock.type(modifier = modifier)
        catchBlocks.forEach {
            type = superTypeOf(type, it.type(modifier = modifier))
        }
        this.type = type
        return type
    }

    fun superTypeOf(vararg types: TypeNode?): TypeNode {
        val types = types.filterNotNull()
        if (types.isEmpty()) throw IllegalArgumentException("superTypeOf input cannot be empty")

        fun superTypeTree(type: DataType): List<DataType> {
            val types = mutableListOf(type)
            if (type !is ObjectType) return types
            var type: ObjectType = type
            while (type.superType != null) {
                type = type.superType!!
                types += type
            }
            return types
        }

        fun superTypeOf(type1: TypeNode, type2: TypeNode): TypeNode {
            if (type1 == type2) return type1
            if (type1.name == type2.name && (type1.isNullable || type2.isNullable)) {
                return type1.toNullable()
            }
            if (type1.name == "Nothing" && type2.name != type1.name) {
                return if (type1.isNullable) {
                    type2.toNullable()
                } else {
                    type2
                }
            }
            if (type2.name == "Nothing" && type2.name != type1.name) {
                return if (type2.isNullable) {
                    type1.toNullable()
                } else {
                    type1
                }
            }

            val typeTree1 = superTypeTree(currentScope.assertToDataType(type1))
            val typeTree2 = superTypeTree(currentScope.assertToDataType(type2))
            val isNullable = type1.isNullable || type2.isNullable
            run {
                typeTree1.forEach { superType1 ->
                    typeTree2.forEach { superType2 ->
                        if (superType1.name == superType2.name) {
                            if (superType1 is ObjectType && superType2 is ObjectType) {
                                if (superType1.arguments.size != superType2.arguments.size) {
                                    return@run
                                }
                                return TypeNode(
                                    position = SourcePosition.NONE,
                                    name = superType1.name,
                                    arguments = superType1.arguments.mapIndexed { index, it ->
                                        superTypeOf(it.toTypeNode(), superType2.arguments[index].toTypeNode())
                                    }.emptyToNull(),
                                    isNullable = isNullable,
                                )
                            } else if (superType1 is FunctionType && superType2 is FunctionType) {
                                if (superType1.arguments.size != superType2.arguments.size) {
                                    return@run
                                }
                                return FunctionTypeNode(
                                    position = SourcePosition.NONE,
                                    parameterTypes = superType1.arguments.mapIndexed { index, it ->
                                        superTypeOf(it.toTypeNode(), superType2.arguments[index].toTypeNode())
                                    },
                                    returnType = superTypeOf(superType1.returnType.toTypeNode(), superType2.returnType.toTypeNode()),
                                    isNullable = isNullable,
                                )
                            } else if (superType1 == superType2) {
                                // types that are not objects nor functions have no type arguments
                                return TypeNode(SourcePosition.NONE, superType1.name, null, isNullable = isNullable)
                            }
                            return@run
                        }
                    }
                }
            }

            return typeRegistry["Any${if (type1.isNullable || type2.isNullable) "?" else ""}"]!!
        }

        var type = types.first()
        types.drop(1)
            .forEach { type = superTypeOf(type, it) }
        return type
    }
}
