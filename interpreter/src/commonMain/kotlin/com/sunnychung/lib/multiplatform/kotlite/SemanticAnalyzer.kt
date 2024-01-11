package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.error.TypeMismatchException
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharType
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleType
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionType
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntType
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.NavigationNode
import com.sunnychung.lib.multiplatform.kotlite.model.NullNode
import com.sunnychung.lib.multiplatform.kotlite.model.NullType
import com.sunnychung.lib.multiplatform.kotlite.model.ObjectType
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyAccessorsNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType.Companion.isLoop
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.SemanticDummyRuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringType
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolReferenceSet
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.ValueNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode
import com.sunnychung.lib.multiplatform.kotlite.model.isNonNullNumberType

class SemanticAnalyzer(val scriptNode: ScriptNode, executionEnvironment: ExecutionEnvironment) {
    val builtinSymbolTable = SymbolTable(scopeLevel = 0, scopeName = ":builtin", scopeType = ScopeType.Script, parentScope = null)
    val symbolTable = SymbolTable(scopeLevel = 1, scopeName = ":global", scopeType = ScopeType.Script, parentScope = builtinSymbolTable)
    var currentScope = symbolTable
    var functionDefIndex = 0
    val symbolRecorders = mutableListOf<SymbolReferenceSet>()

    val typeRegistry = listOf(
        TypeNode("Int", null, false),
        TypeNode("Double", null, false),
        TypeNode("Boolean", null, false),
        TypeNode("String", null, false),
        TypeNode("Char", null, false),
        TypeNode("Unit", null, false),
    )
        .flatMap {
            listOf(it.name to it, "${it.name}?" to it.copy(isNullable = true))
        }
        .let { it + listOf(
            "Null" to TypeNode("Nothing", null, true),
        ) }
        .toMap()

    init {
        listOf("Int", "Double", "Boolean", "String", "Char", "Unit", "Nothing").forEach {
            builtinSymbolTable.declareClass(ClassDefinition(
                currentScope = builtinSymbolTable,
                name = it,
                isInstanceCreationAllowed = false,
                orderedInitializersAndPropertyDeclarations = emptyList(),
                rawMemberProperties = emptyList(),
                memberFunctions = emptyMap(),
                primaryConstructor = null,
            ))
        }

        val libFunctions = executionEnvironment.getBuiltinFunctions(builtinSymbolTable)
        libFunctions.forEach {
                if (it.receiver == null) {
                    builtinSymbolTable.declareFunction(it.name, it)
                } else {
                    builtinSymbolTable.declareExtensionFunction("${it.receiver}/${it.name}", it)
                }
            }

        libFunctions.forEach {
            it.visit()
        }
    }

    fun TypeNode.toNullable() = if (isNullable) {
        this
    } else {
        typeRegistry["$name?"]!!
    }

    fun DataType.toTypeNode(): TypeNode =
        if (this is NullType) {
            typeRegistry["Null"]!!
        } else if (this !is ObjectType && this !is FunctionType) {
            typeRegistry["$name${if (isNullable) "?" else ""}"]!!
        } else if (this is FunctionType) {
            FunctionTypeNode(
                parameterTypes = arguments.map { it.toTypeNode() },
                returnType = returnType.toTypeNode(),
                isNullable = isNullable,
            )
        } else {
            TypeNode(name, null, isNullable)
        }

    fun TypeNode.toDataType(): DataType {
        return currentScope.typeNodeToPropertyType(this, false)?.type
            ?: throw SemanticException("Unknown type `$name`")
    }

    protected fun isLocalAndNotCurrentScope(scopeLevel: Int): Boolean {
        return scopeLevel > 1 && scopeLevel <= (symbolRecorders.lastOrNull()?.scopeLevel ?: currentScope.scopeLevel)
    }

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
            is PropertyAccessorsNode -> TODO()
            is ValueNode -> {}
            is StringLiteralNode -> {}
            is StringNode -> this.visit()
            is LambdaLiteralNode -> this.visit()
            is CharNode -> {}
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
        if (!currentScope.hasProperty(name)) {
            throw SemanticException("Property `$name` is not declared")
        }
        var scope = currentScope
        while (!scope.hasProperty(name, isThisScopeOnly = true)) {
            scope = scope.parentScope!!
        }

        val propertyType = scope.getPropertyType(name).first
        if (!propertyType.isMutable && scope.hasAssignedInThisScope(name)) {
            throw SemanticException("val `$name` cannot be reassigned")
        }
        scope.assign(name, SemanticDummyRuntimeValue(propertyType.type))

        return scope.scopeLevel
    }

    fun findExtensionFunction(receiverType: DataType, functionName: String): FunctionDeclarationNode? {
        return if (receiverType is ObjectType) {
            val clazz = receiverType.clazz
            currentScope.findExtensionFunction("${clazz.fullQualifiedName}/${functionName}")
        } else {
            currentScope.findExtensionFunction("${receiverType.name}/${functionName}")
        }
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
        type()
    }

    fun AssignmentNode.visit() {
        if (subject !is VariableReferenceNode && subject !is NavigationNode) {
            throw SemanticException("$subject cannot be assigned")
        }
        when (subject) {
            is VariableReferenceNode -> {
                subject.visit()
                val variableName = subject.variableName
                val l = checkPropertyWriteAccess(variableName)
                if (transformedRefName == null) {
                    transformedRefName = "$variableName/$l"
                }

//                log.v { "Assign $variableName type ${subject.type()} <- type ${value.type()}" }
            }
            is NavigationNode -> {
                subject.visit()
                // TODO handle NavigationNode

//                log.v { "Assign ${subject.member.name} type ${subject.type()} <- type ${value.type()}" }
            }
            else -> throw SemanticException("$subject cannot be assigned")
        }
        val subjectRawType = subject.type()
        val subjectType = subjectRawType.toDataType()

        if (operator == "=" && subjectRawType is FunctionTypeNode && value is LambdaLiteralNode) {
            value.parameterTypesUpperBound = subjectRawType.parameterTypes
            value.returnTypeUpperBound = subjectRawType.returnType
        }

        value.visit()
        val valueType = value.type().toDataType()

        if (operator in setOf("+=", "-=", "*=", "/=", "%=")) {
            if (operator == "+=" && subjectType is StringType) {
                return // string can concat anything
            }
            if (!valueType.isNonNullNumberType()) {
                throw TypeMismatchException("non-null number type", valueType.nameWithNullable)
            }
            if (subjectType is DoubleType) {
                return // ok
            }
        }

        if (!subjectType.isAssignableFrom(valueType)) {
            throw TypeMismatchException(subjectType.nameWithNullable, valueType.nameWithNullable)
        }
    }

    fun evaluateAndRegisterReturnType(node: ASTNode) {
        val type = node.type()
        if (symbolRecorders.isNotEmpty()) {
            val symbols = symbolRecorders.last()
            val typesToResolve = mutableSetOf(type) + (type.arguments ?: emptyList())
            typesToResolve.forEach {
                val find = currentScope.findClass(it.name)
                if (find != null && isLocalAndNotCurrentScope(find.second.scopeLevel)) {
                    symbols.classes += find.first.fullQualifiedName
                }
            }
        }
    }

    fun PropertyDeclarationNode.visit(isVisitInitialValue: Boolean = true, isClassProperty: Boolean = false, scopeLevel: Int = currentScope.scopeLevel) {
        if (isVisitInitialValue) {
            if (declaredType is FunctionTypeNode && initialValue is LambdaLiteralNode) {
                initialValue.parameterTypesUpperBound = declaredType.parameterTypes
                initialValue.returnTypeUpperBound = declaredType.returnType
            }
            initialValue?.visit()
        }
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException("Property `$name` has already been declared")
        }
        if (!isClassProperty && accessors != null) {
            throw SemanticException("Only class member properties can define custom accessors")
        }
//        if (isMutable && accessors != null) {
//            throw SemanticException("`var` with custom accessors is not supported")
//        }
//        if (accessors?.setter != null) {
//            throw SemanticException("Custom setter is currently not supported")
//        }
        if (isVisitInitialValue && accessors != null) {
            accessors.getter?.visit()
            accessors.setter?.visit()
        }
        if (initialValue != null) {
            val valueType = initialValue.type().toDataType()
            if (declaredType == null) {
                inferredType = initialValue.type()
            }
            val subjectType = type.toDataType()
            if (!subjectType.isAssignableFrom(valueType)) {
                throw TypeMismatchException(subjectType.nameWithNullable, valueType.nameWithNullable)
            }
        } else if (declaredType == null) {
            throw SemanticException("Type cannot be inferred for property `$name`")
        }
        currentScope.declareProperty(name = name, type = type, isMutable = isMutable)
        if (initialValue != null) {
            if (accessors != null) {
                throw SemanticException("Property `$name` with an initial value cannot have custom accessors")
            }

            currentScope.assign(name, SemanticDummyRuntimeValue(currentScope.getPropertyType(name).first.type))
        }
        transformedRefName = "$name/${scopeLevel}"
        currentScope.registerTransformedSymbol(IdentifierClassifier.Property, transformedRefName!!, name)

        evaluateAndRegisterReturnType(this)
    }

    fun VariableReferenceNode.visit() {
        val l = checkPropertyReadAccess(variableName)
        if (variableName != "this" && transformedRefName == null) {
            transformedRefName = "$variableName/$l"
            currentScope.findPropertyOwner(transformedRefName!!)?.let {
                ownerRef = it
            }
            if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(l)) {
                val symbols = symbolRecorders.last()
                symbols.properties += ownerRef ?: transformedRefName!!
            }
        } else if (variableName == "this") {
            if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(l)) {
                val symbols = symbolRecorders.last()
                symbols.properties += variableName
            }
        }

        evaluateAndRegisterReturnType(this)
    }

    fun pushScope(scopeName: String, scopeType: ScopeType, returnType: DataType? = null, overrideScopeLevel: Int = currentScope.scopeLevel + 1) {
        currentScope = SymbolTable(
            scopeLevel = overrideScopeLevel,
            scopeName = scopeName,
            scopeType = scopeType,
            returnType = returnType,
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
            returnType = returnType.toDataType(),
        )

        if (receiver == null) {
            valueParameters.forEach { it.visit() }

            previousScope.declareFunction(name, this)
            if (transformedRefName == null) { // class declaration can assign transformedRefName
                transformedRefName = "$name/${++functionDefIndex}"
            }
            previousScope.registerTransformedSymbol(IdentifierClassifier.Function, transformedRefName!!, name)
        } else {
            currentScope.declareProperty(name = "this", type = TypeNode(receiver, null, false), isMutable = false)
            currentScope.registerTransformedSymbol(IdentifierClassifier.Property, "this", "this")
            val clazz = currentScope.findClass(receiver)?.first ?: throw SemanticException("Class `$receiver` not found")
            clazz.memberProperties.forEach {
                currentScope.declareProperty(name = it.key, type = it.value.type.toTypeNode(), isMutable = it.value.isMutable)
                currentScope.declarePropertyOwner(name = "${it.key}/${currentScope.scopeLevel}", owner = "this/$receiver")
            }
            clazz.memberFunctions.forEach {
                currentScope.declareFunction(name = it.key, node = it.value)
                currentScope.declareFunctionOwner(name = it.key, owner = "this/$receiver")
            }

            valueParameters.forEach { it.visit() }

            previousScope.declareExtensionFunction("$receiver/$name", this)
            transformedRefName = "$receiver/$name/${++functionDefIndex}"
        }

        body.visit()

        // TODO check for return

        val valueType = body.type().toDataType()
        val subjectType = returnType.toDataType()
        if (subjectType !is UnitType && !subjectType.isAssignableFrom(valueType)) {
            throw TypeMismatchException(subjectType.nameWithNullable, valueType.nameWithNullable)
        }

        popScope()

        evaluateAndRegisterReturnType(this)
    }

    fun FunctionCallNode.visit() {
        val functionArgumentDeclarations = when (function) {
            is VariableReferenceNode -> {
                val name = function.variableName
                val arguments = currentScope.findFunction(name)?.also {
                    val owner = currentScope.findFunctionOwner(name)
                    function.ownerRef = owner
                    functionRefName = it.first.transformedRefName

                    if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(it.second.scopeLevel)) {
                        val symbols = symbolRecorders.last()
                        if (owner == null) {
                            symbols.functions += it.first.transformedRefName!!
                        } else {
                            symbols.properties += owner
                        }
                    }
                }?.let { it.first.valueParameters }
                    ?: currentScope.findClass(name)?.also {
                        if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(it.second.scopeLevel)) {
                            val symbols = symbolRecorders.last()
                            symbols.classes += it.first.fullQualifiedName
                        }
                    }?.let { it.first.primaryConstructor?.parameters?.map { it.parameter } ?: emptyList() }
                    ?: currentScope.getPropertyTypeOrNull(name)?.takeIf { it.first.type is FunctionType }?.also {
                        val l = checkPropertyReadAccess(name)
                        if (function.transformedRefName == null) {
                            function.transformedRefName = "$name/$l"
                            currentScope.findPropertyOwner(function.transformedRefName!!)?.let {
                                function.ownerRef = it
                            }

                            if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(l)) {
                                val symbols = symbolRecorders.last()
                                if (function.ownerRef == null) {
                                    symbols.functions += function.transformedRefName!!
                                } else {
                                    symbols.properties += function.ownerRef!!
                                }
                            }
                        }
                    }?.let { (it.first.type as FunctionType).arguments }
                    ?: throw SemanticException("Function $name not found")

                log.v { "Type of call $name -> ${function.type()}" }
                arguments
            }

            is NavigationNode -> {
                function.visit()
                val receiverType = function.subject.type().toDataType()
                var arguments: List<FunctionValueParameterNode> = emptyList()
                if (receiverType is ObjectType) {
                    val clazz = receiverType.clazz
                    val memberFunction = clazz.memberFunctions[function.member.name]
                    if (memberFunction == null) {
                        val extensionFunction = findExtensionFunction(receiverType, function.member.name)
                        if (extensionFunction != null) {
                            functionRefName = extensionFunction.transformedRefName
                            arguments = extensionFunction.valueParameters
                        } else {
                            throw SemanticException("Function `${function.member.name}` not found for type ${receiverType.name}")
                        }
                    } else {
                        arguments = memberFunction.valueParameters
                        functionRefName = memberFunction.transformedRefName
                    }
                } else {
                    // receiverType is a built-in type
                    val extensionFunction = findExtensionFunction(receiverType, function.member.name)
                    if (extensionFunction != null) {
                        functionRefName = extensionFunction.transformedRefName
                        arguments = extensionFunction.valueParameters
                    } else {
                        throw SemanticException("Function `${function.member.name}` not found for type ${receiverType.name}")
                    }
                }

                log.v { "Type of call ${function.member.name} -> ${function.type()}" }
                arguments
            }

            else -> {
                function.visit()
                val type = function.type()
                if (type is FunctionTypeNode) { // function's return type is FunctionTypeNode
                    type.parameterTypes
                } else {
                    throw SemanticException("${type.descriptiveName()} is not callable")
                }
            }
        }

        // Validate call arguments against declared arguments
        // Check for missing mandatory arguments, extra arguments, duplicated arguments and mismatch data types
        if (arguments.size > functionArgumentDeclarations.size) {
            throw SemanticException("Too much arguments. At most ${functionArgumentDeclarations.size} are accepted.")
        }
        arguments.forEachIndexed { i, _ ->
            arguments.forEachIndexed { j, _ ->
                if (i < j && arguments[i].name != null && arguments[j].name != null && arguments[i].name == arguments[j].name) {
                    throw SemanticException("Duplicated argument ${arguments[i].name}")
                }
            }
        }

        class ArgumentInfo(val type: DataType, val isOptional: Boolean, val name: String?)
        val argumentInfos = functionArgumentDeclarations.map {
            when (it) {
                is DataType -> ArgumentInfo(it, false, null)
                is TypeNode -> ArgumentInfo(it.toDataType(), false, null)
                is FunctionValueParameterNode -> ArgumentInfo(it.type.toDataType(), it.defaultValue != null, it.name)
                else -> throw UnsupportedOperationException("Unknown internal class ${it::class.simpleName}")
            }
        }
        val mandatoryArgumentIndexes = argumentInfos.indices.filter { !argumentInfos[it].isOptional }

        val callArgumentMappedIndexes = arguments.mapIndexed { i, a ->
            if (a.name == null) {
                i
            } else {
                argumentInfos.indexOfFirst { it.name == a.name }.also {
                    if (it < 0) throw SemanticException("Argument ${a.name} not found")
                }
            }
        }
        if (callArgumentMappedIndexes.distinct().size < callArgumentMappedIndexes.size) {
            throw SemanticException("There are duplicated arguments")
        }
        val missingIndexes = mandatoryArgumentIndexes.filter { i -> callArgumentMappedIndexes.none { it == i } }
        if (missingIndexes.isNotEmpty()) {
            throw SemanticException("Missing mandatory arguments for index $missingIndexes")
        }
        arguments.forEachIndexed { i, callArgument ->
            val functionArgumentType = argumentInfos[callArgumentMappedIndexes[i]].type
            if (callArgument.value is LambdaLiteralNode && functionArgumentType is FunctionType) {
                if (callArgument.value.valueParameters.size != functionArgumentType.arguments.size) {
                    throw SemanticException("Lambda argument count is different from function parameter declaration.")
                }
                callArgument.value.parameterTypesUpperBound = functionArgumentType.arguments.map {
                    it.toTypeNode()
                }
                callArgument.value.returnTypeUpperBound = functionArgumentType.returnType.toTypeNode()
            }
        }

        // visit argument must before evaluating type
        arguments.forEach { it.visit() }

        arguments.forEachIndexed { i, callArgument ->
            val functionArgumentType = argumentInfos[callArgumentMappedIndexes[i]].type
            if (!functionArgumentType.isAssignableFrom(callArgument.type().toDataType())) {
                throw SemanticException("Argument type ${callArgument.type().descriptiveName()} cannot be mapped to ${argumentInfos[callArgumentMappedIndexes[i]].type.nameWithNullable}")
            }
        }

        // record types if there is any enclosing lambda
        evaluateAndRegisterReturnType(this)
    }

    fun FunctionValueParameterNode.visit() {
        defaultValue?.visit()
        if (currentScope.hasProperty(name = name, isThisScopeOnly = true)) {
            throw SemanticException("Property `$name` has already been declared")
        }
        if (defaultValue != null) {
            val valueType = defaultValue.type().toDataType()
            val subjectType = type.toDataType()
            if (!subjectType.isAssignableFrom(valueType)) {
                throw TypeMismatchException(subjectType.nameWithNullable, valueType.nameWithNullable)
            }
        }
        if (declaredType == null && inferredType == null) {
            throw SemanticException("Cannot infer lambda parameter type")
        }
        currentScope.declareProperty(name = name, type = type, isMutable = false)
        currentScope.assign(name, SemanticDummyRuntimeValue(currentScope.typeNodeToPropertyType(type, false)!!.type))
        transformedRefName = "$name/${currentScope.scopeLevel}"
        currentScope.registerTransformedSymbol(IdentifierClassifier.Property, transformedRefName!!, name)
    }

    fun FunctionCallArgumentNode.visit() {
        value.visit()
    }

    fun BlockNode.visit() {
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
            it.visit()
        }

        returnType = type()

        popScope()
    }

    fun ReturnNode.visit() {
        var s = currentScope
        while (s.scopeType != ScopeType.Function) {
            if (s.scopeType == ScopeType.Script || s.parentScope == null) {
                throw SemanticException("`return` statement should be within a function")
            }
            s = s.parentScope!!
        }
        // TODO block return in lambda
        // s.scopeType == ScopeType.Function
        val declaredReturnType = s.returnType!!
        if (declaredReturnType is FunctionType && value is LambdaLiteralNode) {
            value.parameterTypesUpperBound = declaredReturnType.arguments.map { it.toTypeNode() }
            value.returnTypeUpperBound = declaredReturnType.returnType.toTypeNode()
        }

        value?.visit()
        val valueType = value?.type()?.toDataType() ?: UnitType()
        if (!declaredReturnType.isAssignableFrom(valueType)) {
            throw TypeMismatchException(s.returnType!!.nameWithNullable, valueType.nameWithNullable)
        }
    }

    fun checkBreakOrContinueScope() {
        var s = currentScope
        while (!s.scopeType.isLoop()) {
            if (s.scopeType in setOf(ScopeType.Script, ScopeType.Function) || s.parentScope == null) {
                throw SemanticException("`break` statement should be within a loop")
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
        subject.visit()

        // at this moment subject must not be a primitive
        member.visit()
    }

    fun ClassDeclarationNode.visit() {
        val fullQualifiedClassName = name
        pushScope(name, ScopeType.Class)
        run {
            primaryConstructor?.visit()
            val nonPropertyArguments = primaryConstructor?.parameters
                ?.filter { !it.isProperty }
                ?.map { it.parameter }
            primaryConstructor?.parameters?.forEach {
                currentScope.undeclareProperty(it.parameter.name)
                currentScope.unregisterTransformedSymbol(IdentifierClassifier.Property, it.parameter.transformedRefName!!)
            }
            primaryConstructor?.parameters
                ?.filter { it.isProperty }
                ?.forEach {
                    val p = it.parameter
                    currentScope.declareProperty(name = p.name, type = p.type, isMutable = it.isMutable)
                    currentScope.assign(p.name, SemanticDummyRuntimeValue(currentScope.getPropertyType(p.name).first.type))
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
                                declaredType = p.type,
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
            currentScope.registerTransformedSymbol(IdentifierClassifier.Property, "this", "this")
            primaryConstructor?.parameters
                ?.filter { it.isProperty }
                ?.map { it.parameter }
                ?.forEach { currentScope.declarePropertyOwner(it.transformedRefName!!, "this/$fullQualifiedClassName") }
            declarations.filter { it is ClassInstanceInitializerNode || it is PropertyDeclarationNode }
                .forEach {
                    pushScope("init-property", ScopeType.ClassInitializer)
                    nonPropertyArguments?.forEach { it.copy().visit() }
                    pushScope("init-property-inner", ScopeType.ClassInitializer)
                    if (it is PropertyDeclarationNode) {
                        it.visit(isClassProperty = true)
                    } else {
                        it.visit()
                    }
                    popScope()
                    popScope()
                    if (it is PropertyDeclarationNode) {
                        it.visit(isVisitInitialValue = false, isClassProperty = true)
                        currentScope.declarePropertyOwner(it.transformedRefName!!, "this/$fullQualifiedClassName")
                    }
                }

            declarations.filterIsInstance<FunctionDeclarationNode>()
                .forEach {
                    it.transformedRefName = "${it.name}/${++functionDefIndex}"
                    currentScope.declareFunctionOwner(it.name, "this/$fullQualifiedClassName")
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

    fun StringNode.visit() {
        nodes.forEach { it.visit() }
    }

    fun LambdaLiteralNode.visit() {
//        val type = type() as FunctionTypeNode

        symbolRecorders += SymbolReferenceSet(scopeLevel = currentScope.scopeLevel)
        pushScope(
            scopeName = "<lambda>",
            scopeType = ScopeType.Closure,
            returnType = returnTypeUpperBound?.toDataType() //type.returnType.toDataType(),
        )

        if (parameterTypesUpperBound != null) {
            if (valueParameters.size != parameterTypesUpperBound!!.size) {
                throw SemanticException("Lambda argument count is different from function parameter declaration.")
            }
            valueParameters.forEachIndexed { i, lambdaParameterNode ->
                if (lambdaParameterNode.declaredType == null) {
                    lambdaParameterNode.inferredType = parameterTypesUpperBound!![i]
                }
            }
        }

        valueParameters.forEach {
            if (it.name != "_") {
                it.visit()
            }
        }
        // TODO provide receiver to scope if exists

        body.returnTypeUpperBound = returnTypeUpperBound
        body.visit()

        if (returnTypeUpperBound?.toDataType()?.isAssignableFrom(body.type().toDataType()) == false) {
            throw SemanticException("Lambda return type ${body.type().descriptiveName()} cannot be converted to ${returnTypeUpperBound!!.descriptiveName()}")
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
        }
        type()
    }

    fun analyze() = scriptNode.visit()

    ////////////////////////////////////

    fun ASTNode.type()
        = when (this) {
            is AssignmentNode -> typeRegistry["Unit"]!!
            is BinaryOpNode -> this.type()
            is UnaryOpNode -> this.type()
            is BlockNode -> this.type()
            is BreakNode -> typeRegistry["Unit"]!!
            is ClassDeclarationNode -> typeRegistry["Unit"]!!
            is ClassInstanceInitializerNode -> TODO()
            is ClassMemberReferenceNode -> TODO()
            is ClassParameterNode -> TODO()
            is ClassPrimaryConstructorNode -> TODO()
            is ContinueNode -> typeRegistry["Unit"]!!
            is FunctionCallArgumentNode -> this.type()
            is FunctionCallNode -> this.type()
            is FunctionDeclarationNode -> typeRegistry["Unit"]!!
            is FunctionValueParameterNode -> type
            is IfNode -> this.type()
            is NavigationNode -> this.type()
            is PropertyAccessorsNode -> this.type
            is PropertyDeclarationNode -> typeRegistry["Unit"]!!
            is ReturnNode -> this.type()
            is ScriptNode -> TODO()
            is TypeNode -> this
            is ValueNode -> TODO()
            is VariableReferenceNode -> this.type()
            is WhileNode -> typeRegistry["Unit"]!!

            is IntegerNode -> typeRegistry["Int"]!!
            is DoubleNode -> typeRegistry["Double"]!!
            is BooleanNode -> typeRegistry["Boolean"]!!
            NullNode -> typeRegistry["Null"]!!
            is StringLiteralNode -> TODO()
            is StringNode -> typeRegistry["String"]!!
            is LambdaLiteralNode -> this.type()
            is CharNode -> typeRegistry["Char"]!!
        }

    fun BinaryOpNode.type(): TypeNode = type ?: when (operator) {
        "+", "-", "*", "/", "%" -> {
            val t1 = node1.type().toDataType()
            val t2 = node2.type().toDataType()
            if (t1 is StringType || t1 is NullType || t2 is StringType || t2 is NullType) {
                typeRegistry["String"]!!
            } else if ((t1 == DoubleType(isNullable = false) && t2.isNonNullNumberType())
                || (t2 == DoubleType(isNullable = false) && t2.isNonNullNumberType())
            ) {
                typeRegistry["Double"]!!
            } else if (t1 == IntType(isNullable = false) && t2 == IntType(isNullable = false)) {
                typeRegistry["Int"]!!
            } else if (operator == "+" && t1 is CharType && t2 is IntType) {
                typeRegistry["Char"]!!
            } else if (operator == "-" && t1 is CharType && t2 is CharType) {
                typeRegistry["Int"]!!
            } else {
                throw SemanticException("Types ${t1.nameWithNullable} and ${t2.nameWithNullable} cannot be applied with operator `$operator`")
            }
        }

        "<", "<=", ">", ">=", "==", "!=", "||", "&&" -> typeRegistry["Boolean"]!!

        else -> throw UnsupportedOperationException()
    }.also {
        type = it
    }

    fun UnaryOpNode.type(): TypeNode = type ?: node!!.type().also { type = it }

    // e.g. `name()`, where name is a VariableReferenceNode
    fun VariableReferenceNode.type() = type ?: (
            currentScope.findClass(variableName)
                ?.let { FunctionTypeNode(parameterTypes = emptyList(), returnType = TypeNode(variableName, null, false), isNullable = false) }
                ?: currentScope.findFunction(variableName)?.first?.let { FunctionTypeNode(parameterTypes = emptyList(), returnType = it.returnType, isNullable = false) }
                ?: currentScope.getPropertyType(variableName).first.type.toTypeNode()!!
            ).also { type = it }

    fun NavigationNode.type(): TypeNode {
        type?.let { return it }
        val subjectType = when(val type = subject.type()) {
            is FunctionTypeNode -> type.returnType
            else -> type
        }
        val clazz = currentScope.findClass(subjectType.name)?.first ?: throw SemanticException("Unknown type `${subjectType.name}`")
        val memberName = member.name
        clazz.memberFunctions[memberName]?.let {
            return FunctionTypeNode(parameterTypes = emptyList(), returnType = it.returnType, isNullable = false).also { type = it }
        }
        clazz.memberPropertyCustomAccessors[memberName]?.let {
            return it.type().also { type = it }
        }
        clazz.memberProperties[memberName]?.let {
            return it.type.toTypeNode().also { type = it }
        }

        findExtensionFunction(subjectType.toDataType(), memberName)?.let {
            return FunctionTypeNode(parameterTypes = emptyList(), returnType = it.returnType, isNullable = false).also { type = it }
        }

        throw SemanticException("Could not find member `$memberName` for type ${clazz.name}")
    }

    fun FunctionCallArgumentNode.type(): TypeNode {
        return this.value.type()
    }

    fun FunctionCallNode.type(): TypeNode {
        returnType?.let { return it }
        val functionType = function.type()
        if (functionType !is FunctionTypeNode) {
            throw SemanticException("Cannot invoke non-function expression")
        }
        return functionType.returnType
    }

    fun ReturnNode.type(): TypeNode {
        return value?.type() ?: typeRegistry["Unit"]!!
    }

    fun IfNode.type(): TypeNode {
        type?.let { return it }
        return superTypeOf(trueBlock?.type(), falseBlock?.type())
            .also { type = it }
    }

    fun BlockNode.type(): TypeNode {
        returnType?.let { return it }
        return (statements.lastOrNull()?.type() ?: typeRegistry["Unit"]!!)
            .also { returnType = it }
    }

    fun LambdaLiteralNode.type(): TypeNode {
        type?.let { return it }
        return FunctionTypeNode(parameterTypes = valueParameters.map { it.type() }, returnType = body.type(), isNullable = false)
            .also { type = it }
    }

    fun superTypeOf(vararg types: TypeNode?): TypeNode {
        val types = types.filterNotNull()
        if (types.isEmpty()) throw IllegalArgumentException("superTypeOf input cannot be empty")

        fun superTypeOf(type1: TypeNode, type2: TypeNode): TypeNode {
            if (type1 == type2) return type1
            if (type1.name == type2.name && (type1.isNullable || type2.isNullable)) {
                return type1.toNullable()
            }
            if (type1.name == "Nothing" && type2.name != type1.name) {
                return type2.toNullable()
            }
            if (type2.name == "Nothing" && type2.name != type1.name) {
                return type1.toNullable()
            }
            // TODO return "Any"
            throw SemanticException("Cannot find super type of ${type1.descriptiveName()} and ${type2.descriptiveName()}")
        }

        var type = types.first()
        types.drop(1)
            .forEach { type = superTypeOf(type, it) }
        return type
    }
}
