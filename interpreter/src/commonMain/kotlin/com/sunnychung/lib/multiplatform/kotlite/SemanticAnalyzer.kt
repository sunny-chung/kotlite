package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.error.TypeMismatchException
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.CallableType
import com.sunnychung.lib.multiplatform.kotlite.model.CharNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharType
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleType
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentInfo
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
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyOwnerInfo
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType.Companion.isLoop
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.SemanticAnalyzerSymbolTable
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
import com.sunnychung.lib.multiplatform.kotlite.model.toSignature

class SemanticAnalyzer(val scriptNode: ScriptNode, executionEnvironment: ExecutionEnvironment) {
    val builtinSymbolTable = SemanticAnalyzerSymbolTable(scopeLevel = 0, scopeName = ":builtin", scopeType = ScopeType.Script, parentScope = null)
    val symbolTable = SemanticAnalyzerSymbolTable(scopeLevel = 1, scopeName = ":global", scopeType = ScopeType.Script, parentScope = builtinSymbolTable)
    var currentScope = builtinSymbolTable
    var functionDefIndex = 0
    val symbolRecorders = mutableListOf<SymbolReferenceSet>()

    val typeRegistry = listOf(
        TypeNode("Any", null, false),
        TypeNode("Int", null, false),
        TypeNode("Double", null, false),
        TypeNode("Boolean", null, false),
        TypeNode("String", null, false),
        TypeNode("Char", null, false),
        TypeNode("Unit", null, false),
    )
        .flatMap {
            listOf(
                it.name to it,
                "${it.name}?" to it.copy(isNullable = true),
            )
        }
        .let { it + listOf(
            "Null" to TypeNode("Nothing", null, true),
        ) }
        .toMap()

    init {
        executionEnvironment.getBuiltinClasses(builtinSymbolTable).forEach {
            builtinSymbolTable.declareClass(it)
        }

        executionEnvironment.getExtensionProperties(builtinSymbolTable).forEach {
            Parser(Lexer(it.type)).type().also { type ->
                it.typeNode = type
            }
            it.transformedName = "EP//${it.receiver}/${it.declaredName}/${++functionDefIndex}"
            builtinSymbolTable.declareExtensionProperty(it.transformedName!!, it)
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

    fun TypeNode.unboxClassTypeAsCompanion() = if (this is ClassTypeNode) {
        TypeNode("${this.clazz.name}.Companion", this.clazz.arguments, false)
    } else {
        this
    }

    protected fun isLocalAndNotCurrentScope(scopeLevel: Int): Boolean {
        return scopeLevel > 1 && scopeLevel <= (symbolRecorders.lastOrNull()?.scopeLevel ?: currentScope.scopeLevel)
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
            is FunctionValueParameterNode -> this.visit(modifier = modifier)
            is IntegerNode -> {}
            is DoubleNode -> {}
            is BooleanNode -> {}
            is NullNode -> {}
            is PropertyDeclarationNode -> this.visit(modifier = modifier)
            is ScriptNode -> this.visit(modifier = modifier)
            is TypeNode -> {}
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
            is PropertyAccessorsNode -> TODO()
            is ValueNode -> {}
            is StringLiteralNode -> {}
            is StringNode -> this.visit(modifier = modifier)
            is LambdaLiteralNode -> this.visit(modifier = modifier)
            is CharNode -> {}
        }
    }

    fun checkPropertyReadAccess(name: String): Int {
        if (!currentScope.hasProperty(name)) {
            throw SemanticException("Property `$name` is not declared")
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
    fun checkPropertyWriteAccess(name: String): Int {
        if (!currentScope.hasProperty(name)) {
            throw SemanticException("Property `$name` is not declared")
        }
        var scope: SymbolTable = currentScope
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

    fun ScriptNode.visit(modifier: Modifier = Modifier()) {
        nodes.forEach { it.visit(modifier = modifier) }
    }

    fun UnaryOpNode.visit(modifier: Modifier = Modifier()) {
        node!!.visit(modifier = modifier)
        if (operator in setOf("pre++", "pre--", "post++", "post--") && node is VariableReferenceNode) {
            checkPropertyWriteAccess((node as VariableReferenceNode).variableName)
        }
    }

    fun BinaryOpNode.visit(modifier: Modifier = Modifier()) {
        node1.visit(modifier = modifier)
        node2.visit(modifier = modifier)
        type()
    }

    fun AssignmentNode.visit(modifier: Modifier = Modifier()) {
        if (subject !is VariableReferenceNode && subject !is NavigationNode) {
            throw SemanticException("$subject cannot be assigned")
        }
        when (subject) {
            is VariableReferenceNode -> {
                subject.visit(modifier = modifier)
                val variableName = subject.variableName
                val l = checkPropertyWriteAccess(variableName)
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
            else -> throw SemanticException("$subject cannot be assigned")
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

    fun PropertyDeclarationNode.visit(modifier: Modifier = Modifier(), isVisitInitialValue: Boolean = true, isClassProperty: Boolean = false, scopeLevel: Int = currentScope.scopeLevel) {
        if (isVisitInitialValue) {
            if (declaredType is FunctionTypeNode && initialValue is LambdaLiteralNode) {
                initialValue.parameterTypesUpperBound = declaredType.parameterTypes
                initialValue.returnTypeUpperBound = declaredType.returnType
            }
            initialValue?.visit(modifier = modifier)
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
            accessors.getter?.visit(modifier = modifier)
            accessors.setter?.visit(modifier = modifier)
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

    fun VariableReferenceNode.visit(modifier: Modifier = Modifier()) {
        if (!currentScope.hasProperty(variableName)) {
            if (currentScope.findClass(variableName) != null) {
                return
            }
        }
        val l = checkPropertyReadAccess(variableName)
        if (variableName != "this" && transformedRefName == null) {
            transformedRefName = "$variableName/$l"
            currentScope.findPropertyOwner(transformedRefName!!)?.let {
                ownerRef = it
            }
            if (symbolRecorders.isNotEmpty() && isLocalAndNotCurrentScope(l)) {
                val symbols = symbolRecorders.last()
                symbols.properties += ownerRef?.ownerRefName ?: transformedRefName!!
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

    fun FunctionDeclarationNode.visit(modifier: Modifier = Modifier()) {
        val previousScope = currentScope
        var additionalScopeCount = 0
        pushScope(
            scopeName = name,
            scopeType = ScopeType.Function,
            returnType = returnType.toDataType(),
        )
        ++additionalScopeCount

        if (receiver == null) {
            valueParameters.forEach { it.visit(modifier = modifier) }

            previousScope.declareFunction(name, this)
            if (transformedRefName == null) { // class declaration can assign transformedRefName
                transformedRefName = "$name/${++functionDefIndex}"
            }
            previousScope.registerTransformedSymbol(IdentifierClassifier.Function, transformedRefName!!, name)
        } else {
            val typeNode = TypeNode(receiver.trimEnd('?'), null, receiver.endsWith("?"))
            currentScope.declareProperty(name = "this", type = typeNode, isMutable = false)
            currentScope.registerTransformedSymbol(IdentifierClassifier.Property, "this", "this")
            val clazz = currentScope.findClass(typeNode.name)?.first ?: throw SemanticException("Class `$receiver` not found")
            if (!typeNode.isNullable) {
                clazz.memberProperties.forEach {
                    currentScope.declareProperty(
                        name = it.key,
                        type = it.value.type.toTypeNode(),
                        isMutable = it.value.isMutable
                    )
                    currentScope.declarePropertyOwner(
                        name = "${it.key}/${currentScope.scopeLevel}",
                        owner = "this/$receiver"
                    )
                }
                clazz.memberFunctions.forEach {
                    currentScope.declareFunction(name = it.key, node = it.value)
                    currentScope.declareFunctionOwner(name = it.key, function = it.value, owner = "this/$receiver")
                }
            }
            currentScope.findExtensionPropertyByReceiver(typeNode.descriptiveName()).forEach {
                currentScope.declareProperty(
                    name = it.second.declaredName,
                    type = it.second.typeNode!!,
                    isMutable = it.second.setter != null
                )
                currentScope.declarePropertyOwner(
                    name = "${it.second.declaredName}/${currentScope.scopeLevel}",
                    owner = "this/$receiver",
                    extensionPropertyRef = it.first,
                )
            }

            pushScope("$name(valueParameters)", ScopeType.FunctionParameters)
            ++additionalScopeCount

            valueParameters.forEach { it.visit(modifier = modifier) }

            // 1. setting `transformedRefName` must be before `this.copy()`.
            // 2. `transformedRefName` should be identical among these extension functions, because
            //    only one implementation would be registered in Interpreter.
            transformedRefName = "${typeNode.descriptiveName()}/$name/${++functionDefIndex}"
            previousScope.declareExtensionFunction("${typeNode.name}/$name", this.copy(receiver = receiver.trimEnd('?')))

            if (typeNode.isNullable) {
                previousScope.declareExtensionFunction("${typeNode.name}?/$name", this)
//                transformedRefName = "${typeNode.name}?/$name/${++functionDefIndex}"

                previousScope.declareExtensionFunction("Nothing/$name", this.copy(receiver = "Nothing"))
//                transformedRefName = "Nothing/$name/${++functionDefIndex}"
            }
        }

        body.visit(modifier = modifier)

        // TODO check for return statement

        val valueType = body.type().toDataType()
        val subjectType = returnType.toDataType()
        if (subjectType !is UnitType && !subjectType.isAssignableFrom(valueType)) {
            throw TypeMismatchException(subjectType.nameWithNullable, valueType.nameWithNullable)
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
                    throw SemanticException("Duplicated argument ${arguments[i].name}")
                }
            }
        }

        // visit argument must before evaluating type
        arguments.forEach {
            it.visit(modifier = modifier.copy(isSkipGenerics = true))
        }

        val functionArgumentAndReturnTypeDeclarations = when (function) {
            is VariableReferenceNode -> {
                val resolution = currentScope.findMatchingCallables(
                    currentSymbolTable = currentScope,
                    originalName = function.variableName,
                    receiverType = null,
                    arguments = arguments.map { FunctionCallArgumentInfo(it.name, it.type(ResolveTypeModifier(isSkipGenerics = true)).toDataType()) }
                )
                    .firstOrNull()
                    ?: throw SemanticException("No matching function `${function.variableName}` found")
                function.ownerRef = resolution.owner?.let { PropertyOwnerInfo(it) }
                functionRefName = resolution.transformedName
                callableType = resolution.type

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

                resolution.arguments to resolution.returnType
            }

            is NavigationNode -> {
                function.visit(modifier = modifier, IdentifierClassifier.Function)
                val receiverType = function.subject.type().unboxClassTypeAsCompanion().toDataType()
                val lookupReceiverTypes = if (!receiverType.isNullable || function.operator == ".") {
                    listOf(receiverType)
                } else { // ?.
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
                        arguments.map { FunctionCallArgumentInfo(it.name, it.type().toDataType()) }
                    )
                }
                    .distinct()
                if (resolutions.size > 1) {
                    throw SemanticException("Ambiguous function call for `${function.member.name}`. ${resolutions.size} candidates match.")
                }
                val resolution = resolutions.firstOrNull() ?: throw SemanticException("No matching function `${function.member.name}` found for type ${receiverType.nameWithNullable}")

                functionRefName = resolution.transformedName
                callableType = resolution.type

                /**
                 * Return type must be nullable if:
                 * - Callable returns nullable type
                 * - receiver is nullable and operator is "?."
                 */

                val returnType = resolution.returnType.let {
                    if (receiverType.isNullable && function.operator == "?.") {
                        it.copy(isNullable = true)
                    } else {
                        it
                    }
                }

                resolution.arguments to returnType
            }

            else -> {
                function.visit(modifier = modifier)
                val type = function.type()
                if (type is FunctionTypeNode) { // function's return type is FunctionTypeNode
                    if (!type.isNullable) {
                        type.parameterTypes!! to type.returnType
                    } else {
                        throw SemanticException("${type.descriptiveName()} is not callable")
                    }
                } else {
                    throw SemanticException("${type.descriptiveName()} is not callable")
                }
            }
        }

        // Validate call arguments against declared arguments
        // Check for missing mandatory arguments, extra arguments, duplicated arguments and mismatch data types
//        if (arguments.size > functionArgumentDeclarations.size) {
//            throw SemanticException("Too much arguments. At most ${functionArgumentDeclarations.size} are accepted.")
//        }

        class ArgumentInfo(val type: DataType, val isOptional: Boolean, val name: String?)
        val argumentInfos = functionArgumentAndReturnTypeDeclarations.first.map {
            when (it) {
                is DataType -> ArgumentInfo(it, false, null)
                is TypeNode -> ArgumentInfo(it.toDataType(), false, null)
                is FunctionValueParameterNode -> ArgumentInfo(it.type.toDataType(), it.defaultValue != null, it.name)
                else -> throw UnsupportedOperationException("Unknown internal class ${it::class.simpleName}")
            }
        }
//        val mandatoryArgumentIndexes = argumentInfos.indices.filter { !argumentInfos[it].isOptional }
//
        val callArgumentMappedIndexes = arguments.mapIndexed { i, a ->
            if (a.name == null) {
                i
            } else {
                argumentInfos.indexOfFirst { it.name == a.name }.also {
                    if (it < 0) throw SemanticException("Argument ${a.name} not found")
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

        // revisit to resolve generic type parameters
        // visit argument must before evaluating type
        arguments.forEach { it.visit(modifier = modifier) }

        // check for mismatch generic parameters
        arguments.forEachIndexed { i, callArgument ->
            val functionArgumentType = argumentInfos[callArgumentMappedIndexes[i]].type
            if (!functionArgumentType.isAssignableFrom(callArgument.type().toDataType())) {
                throw SemanticException("Argument type ${callArgument.type().descriptiveName()} cannot be mapped to ${argumentInfos[callArgumentMappedIndexes[i]].type.nameWithNullable}")
            }
        }

        returnType = functionArgumentAndReturnTypeDeclarations.second!!

        // record types if there is any enclosing lambda
        evaluateAndRegisterReturnType(this)
    }

    fun FunctionValueParameterNode.visit(modifier: Modifier = Modifier()) {
        defaultValue?.visit(modifier = modifier)
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

        value?.visit(modifier = modifier)
        val valueType = value?.type()?.toDataType() ?: UnitType()
        if (!declaredReturnType.isAssignableFrom(valueType)) {
            throw TypeMismatchException(s.returnType!!.nameWithNullable, valueType.nameWithNullable)
        }
    }

    fun checkBreakOrContinueScope() {
        var s: SymbolTable = currentScope
        while (!s.scopeType.isLoop()) {
            if (s.scopeType in setOf(ScopeType.Script, ScopeType.Function) || s.parentScope == null) {
                throw SemanticException("`break` statement should be within a loop")
            }
            s = s.parentScope!!
        }
    }

    fun BreakNode.visit(modifier: Modifier = Modifier()) {
        checkBreakOrContinueScope()
    }

    fun ContinueNode.visit(modifier: Modifier = Modifier()) {
        checkBreakOrContinueScope()
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
        val memberName = member.name
        val clazz = subjectType.nameWithNullable.let { currentScope.findClass(it) ?: throw RuntimeException("Cannot find class `$it`") }.first
        if (lookupType == IdentifierClassifier.Property) {
            if (clazz.memberProperties.containsKey(memberName)) {
                if (isCheckWriteAccess && !clazz.memberProperties[memberName]!!.isMutable) {
                    throw SemanticException("val `$memberName` cannot be reassigned")
                }
                return
            }
            if (clazz.memberPropertyCustomAccessors.containsKey(memberName)) {
                val accessor = clazz.memberPropertyCustomAccessors[memberName]!!
                if (isCheckWriteAccess) {
                    if (accessor.setter == null) {
                        throw SemanticException("Setter for `$memberName` is not declared")
                    }
                } else if (accessor.getter == null) {
                    throw SemanticException("Getter for `$memberName` is not declared")
                }
                return
            }
            currentScope.findExtensionPropertyByDeclaration(subjectType.nameWithNullable, memberName)?.let {
                if (isCheckWriteAccess && it.second.setter == null) {
                    throw SemanticException("Setter for `$memberName` is not declared")
                } else if (!isCheckWriteAccess && it.second.getter == null) {
                    throw SemanticException("Getter for `$memberName` is not declared")
                }
                transformedRefName = it.first
                return
            }
        } else {
            if (clazz.findMemberFunctionsByDeclaredName(memberName).isNotEmpty()) return
            if (currentScope.findExtensionFunctionsByOriginalName(subjectType.nameWithNullable, memberName).isNotEmpty()) return
        }
        throw SemanticException("Type `${subjectType.nameWithNullable}` has no member `$memberName`")
    }

    fun ClassDeclarationNode.visit(modifier: Modifier = Modifier()) {
        val fullQualifiedClassName = name

        // Declare nullable class type
        currentScope.parentScope!!.declareClass(
            ClassDefinition(
                currentScope = currentScope,
                name = "$name?",
                fullQualifiedName = "$fullQualifiedClassName?",
                isInstanceCreationAllowed = false,
                orderedInitializersAndPropertyDeclarations = emptyList(),
                rawMemberProperties = emptyList(),
                memberFunctions = emptyMap(),
                primaryConstructor = null,
            )
        )

        // Declare companion object
        currentScope.parentScope!!.declareClass(
            ClassDefinition(
                currentScope = currentScope,
                name = "$name.Companion",
                fullQualifiedName = "$fullQualifiedClassName.Companion",
                isInstanceCreationAllowed = false,
                orderedInitializersAndPropertyDeclarations = emptyList(),
                rawMemberProperties = emptyList(),
                memberFunctions = emptyMap(),
                primaryConstructor = null,
            )
        )

        pushScope(name, ScopeType.Class)
        run {
            primaryConstructor?.visit(modifier = modifier)
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
                                receiver = fullQualifiedClassName,
                                declaredType = p.type,
                                isMutable = it.isMutable,
                                initialValue = p.defaultValue,
                                transformedRefName = p.transformedRefName,
                            )
                        } ?: emptyList()) +
                            declarations.filterIsInstance<PropertyDeclarationNode>()),
                    memberFunctions = declarations
                        .filterIsInstance<FunctionDeclarationNode>()
                        .associateBy { it.toSignature(currentScope.parentScope as SemanticAnalyzerSymbolTable) },
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
                    nonPropertyArguments?.forEach { it.copy().visit(modifier = modifier) }
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
                        currentScope.declarePropertyOwner(it.transformedRefName!!, "this/$fullQualifiedClassName")
                    }
                }

            declarations.filterIsInstance<FunctionDeclarationNode>()
                .forEach {
                    it.transformedRefName = "${it.name}/${++functionDefIndex}"
                    currentScope.declareFunctionOwner(it.name, it, "this/$fullQualifiedClassName")
                }

            declarations.filterIsInstance<FunctionDeclarationNode>()
                .forEach {
                    it.visit(modifier = modifier)
                }
        }
        popScope()
    }

    fun ClassPrimaryConstructorNode.visit(modifier: Modifier = Modifier()) {
        parameters.forEach { it.visit(modifier = modifier) }
    }

    fun ClassParameterNode.visit(modifier: Modifier = Modifier()) {
        parameter.visit(modifier = modifier)
    }

    fun ClassInstanceInitializerNode.visit(modifier: Modifier = Modifier()) {
        block.visit(modifier = modifier)
    }

    fun ClassMemberReferenceNode.visit(modifier: Modifier = Modifier()) {
        // TODO check for write access
        // check for existence and return error
        try {
            val l = checkPropertyReadAccess(name)
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
                it.visit(modifier = modifier)
            }
        }
        // TODO provide receiver to scope if exists

        body.returnTypeUpperBound = returnTypeUpperBound
        body.visit(modifier = modifier)

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

    data class ResolveTypeModifier(val isSkipGenerics: Boolean = false)

    fun ASTNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier())
        = when (this) {
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
            is NavigationNode -> this.type(modifier = modifier)
            is PropertyAccessorsNode -> this.type
            is PropertyDeclarationNode -> typeRegistry["Unit"]!!
            is ReturnNode -> this.type(modifier = modifier)
            is ScriptNode -> TODO()
            is TypeNode -> this
            is ValueNode -> TODO()
            is VariableReferenceNode -> this.type(modifier = modifier)
            is WhileNode -> typeRegistry["Unit"]!!

            is IntegerNode -> typeRegistry["Int"]!!
            is DoubleNode -> typeRegistry["Double"]!!
            is BooleanNode -> typeRegistry["Boolean"]!!
            NullNode -> typeRegistry["Null"]!!
            is StringLiteralNode -> TODO()
            is StringNode -> typeRegistry["String"]!!
            is LambdaLiteralNode -> this.type(modifier = modifier)
            is CharNode -> typeRegistry["Char"]!!
        }

    fun BinaryOpNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier()): TypeNode = type ?: when (operator) {
        "+", "-", "*", "/", "%" -> {
            val t1 = node1.type(modifier = modifier).toDataType()
            val t2 = node2.type(modifier = modifier).toDataType()
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
                ?.let { ClassTypeNode(TypeNode(variableName, null, false)) }
                ?: currentScope.findFunctionsByOriginalName(variableName).firstOrNull()?.let { FunctionTypeNode(parameterTypes = null, returnType = null, isNullable = false) }
                ?: currentScope.getPropertyType(variableName).first.type.toTypeNode()!!
            ).also { type = it }

    fun NavigationNode.type(modifier: ResolveTypeModifier = ResolveTypeModifier(), lookupType: IdentifierClassifier = IdentifierClassifier.Property): TypeNode {
        type?.let { return it }
        val subjectType = when(val type = subject.type(modifier = modifier)) {
            is FunctionTypeNode -> type.returnType
            is ClassTypeNode -> type.unboxClassTypeAsCompanion()
            else -> type
        } ?: return typeRegistry["Any"]!!
        val clazz = currentScope.findClass(subjectType.name)?.first ?: throw SemanticException("Unknown type `${subjectType.name}`")
        val memberName = member.name
        if (lookupType == IdentifierClassifier.Property) {
            clazz.memberPropertyCustomAccessors[memberName]?.let {
                return it.type(modifier = modifier).also { type = it }
            }
            clazz.memberProperties[memberName]?.let {
                return it.type.toTypeNode().also { type = it }
            }
            transformedRefName?.let {
                currentScope.findExtensionProperty(it)
            }?.let {
                return it.typeNode!!
            }
        } else {
            clazz.memberFunctions[memberName]?.let {
                return FunctionTypeNode(
                    parameterTypes = emptyList(),
                    returnType = it.returnType,
                    isNullable = false
                ).also { type = it }
            }
            findExtensionFunction(subjectType.toDataType(), memberName)?.let {
                return FunctionTypeNode(
                    parameterTypes = emptyList(),
                    returnType = it.returnType,
                    isNullable = false
                ).also { type = it }
            }
        }

        throw SemanticException("Could not find member `$memberName` for type ${clazz.name}")
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
            throw SemanticException("Cannot invoke non-function expression")
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
        if (modifier.isSkipGenerics) return FunctionTypeNode(parameterTypes = null, returnType = null, isNullable = false)
        type?.let { return it }
        return FunctionTypeNode(parameterTypes = valueParameters.map { it.type(modifier = modifier) }, returnType = body.type(), isNullable = false)
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
