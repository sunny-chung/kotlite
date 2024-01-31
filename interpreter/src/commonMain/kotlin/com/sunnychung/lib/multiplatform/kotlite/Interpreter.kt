package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateNullPointerException
import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateRuntimeException
import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateTypeCastException
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalBreakException
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalContinueException
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalReturnException
import com.sunnychung.lib.multiplatform.kotlite.extension.emptyToNull
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterType
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeArguments
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AsOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.CallStack
import com.sunnychung.lib.multiplatform.kotlite.model.CallableNode
import com.sunnychung.lib.multiplatform.kotlite.model.CallableType
import com.sunnychung.lib.multiplatform.kotlite.model.CharNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharValue
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.ComparableRuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallResult
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionType
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaValue
import com.sunnychung.lib.multiplatform.kotlite.model.ListValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongNode
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.NavigationNode
import com.sunnychung.lib.multiplatform.kotlite.model.NullNode
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.NumberValue
import com.sunnychung.lib.multiplatform.kotlite.model.ObjectType
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyAccessorsNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringType
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterType
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue
import com.sunnychung.lib.multiplatform.kotlite.model.ValueNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode

class Interpreter(val scriptNode: ScriptNode, executionEnvironment: ExecutionEnvironment) {

    internal val callStack = CallStack()
    internal val globalScope = callStack.currentSymbolTable()

    init {
        executionEnvironment.getBuiltinClasses(globalScope).forEach {
            callStack.provideBuiltinClass(it)
        }
        executionEnvironment.getBuiltinFunctions(globalScope).forEach {
            callStack.provideBuiltinFunction(it)
        }
        executionEnvironment.getExtensionProperties(globalScope).forEach {
            callStack.provideBuiltinExtensionProperty(it)
        }
    }

    fun symbolTable() = callStack.currentSymbolTable()

    fun ASTNode.eval(): Any {
        return when (this) {
            is AssignmentNode -> this.eval()
            is BinaryOpNode -> this.eval()
            is IntegerNode -> this.eval()
            is LongNode -> this.eval()
            is DoubleNode -> this.eval()
            is BooleanNode -> this.eval()
            is NullNode -> this.eval()
            is PropertyDeclarationNode -> this.eval()
            is ScriptNode -> this.eval()
            is TypeNode -> this.eval()
            is UnaryOpNode -> this.eval()
            is VariableReferenceNode -> this.eval()
            is FunctionDeclarationNode -> this.eval()
            is FunctionValueParameterNode -> throw UnsupportedOperationException()
            is FunctionCallArgumentNode -> throw UnsupportedOperationException()
            is FunctionCallNode -> this.eval()
            is BlockNode -> this.eval()
            is ReturnNode -> this.eval()
            is IfNode -> this.eval()
            is WhileNode -> this.eval()
            is BreakNode -> this.eval()
            is ContinueNode -> this.eval()
            is ClassDeclarationNode -> this.eval()
            is ClassInstanceInitializerNode -> TODO()
            is ClassParameterNode -> TODO()
            is ClassPrimaryConstructorNode -> TODO()
            is ClassMemberReferenceNode -> TODO()
            is NavigationNode -> this.eval()
            is PropertyAccessorsNode -> TODO()
            is ValueNode -> this.eval()
            is StringLiteralNode -> this.eval()
            is StringNode -> this.eval()
            is LambdaLiteralNode -> this.eval()
            is CharNode -> this.eval()
            is AsOpNode -> this.eval()
            is TypeParameterNode -> TODO()
        }
    }

    fun ScriptNode.eval() {
        nodes.forEach { it.eval() }
    }

    fun <T : RuntimeValue, R: RuntimeValue> castType(a: Any, calculation: (T) -> R): R
        = calculation(a as T)

    fun <T : RuntimeValue, R: RuntimeValue> castType(a: Any, b: Any, calculation: (T, T) -> R): R
        = calculation(a as T, b as T)

    fun BinaryOpNode.eval(): RuntimeValue {
        return when (operator) { // TODO overflow
            "+" -> {
                val r1 = node1.eval() as RuntimeValue
                val r2 = node2.eval() as RuntimeValue
                if (r1 is StringValue || r1 is NullValue || r2 is StringValue || r2 is NullValue) {
                    StringValue(r1.convertToString() + r2.convertToString())
                } else if (r1 is CharValue && r2 is IntValue) {
                    CharValue(r1.value + r2.value)
                } else {
                    castType<NumberValue<*>, NumberValue<*>>(r1, r2) { a, b -> a + b }
                }
            }
            "-" -> {
                val r1 = node1.eval() as RuntimeValue
                val r2 = node2.eval() as RuntimeValue
                if (r1 is CharValue && r2 is CharValue) {
                    IntValue(r1.value - r2.value)
                } else {
                    castType<NumberValue<*>, NumberValue<*>>(r1, r2) { a, b -> a - b }
                }
            }
            "*" -> castType<NumberValue<*>, NumberValue<*>>(node1.eval(), node2.eval()) { a, b -> a * b }
            "/" -> castType<NumberValue<*>, NumberValue<*>>(node1.eval(), node2.eval()) { a, b -> a / b }
            "%" -> castType<NumberValue<*>, NumberValue<*>>(node1.eval(), node2.eval()) { a, b -> a % b }

            "<" -> {
//                val r1 = node1.eval() as RuntimeValue
//                val r2 = node2.eval() as RuntimeValue
//                val r1 = node1.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>>
//                val r2 = node2.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>>
//                BooleanValue(r1 < r2)
                BooleanValue(node1.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>> < node2.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>>)
//                if (r1 is )
//                castType<NumberValue<*>, BooleanValue>(node1.eval(), node2.eval()) { a, b -> BooleanValue(a < b) }
            }
            "<=" -> BooleanValue(node1.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>> <= node2.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>>)
            ">" -> BooleanValue(node1.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>> > node2.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>>)
            ">=" -> BooleanValue(node1.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>> >= node2.eval() as ComparableRuntimeValue<Comparable<Comparable<*>>>)
            "==" -> {
                val r1 = node1.eval() as RuntimeValue
                val r2 = node2.eval() as RuntimeValue
//                if (r1 is NullValue || r2 is NullValue) {
//                    return BooleanValue(r1 == r2)
//                }
                if (r1 is NumberValue<*> && r2 is NumberValue<*>) {
                    return castType<NumberValue<*>, BooleanValue>(r1, r2) { a, b -> BooleanValue(a == b) }
                }
                return BooleanValue(r1 == r2)
            }
            "!=" -> {
                val r1 = node1.eval() as RuntimeValue
                val r2 = node2.eval() as RuntimeValue
//                if (r1 is NullValue || r2 is NullValue) {
//                    return BooleanValue(r1 != r2)
//                }
                if (r1 is NumberValue<*> && r2 is NumberValue<*>) {
                    castType<NumberValue<*>, BooleanValue>(r1, r2) { a, b -> BooleanValue(a != b) }
                }
                return BooleanValue(r1 != r2)
            }

            "||" -> castType<BooleanValue, BooleanValue>(node1.eval()) { a -> if (a.value) BooleanValue(true) else node2.eval() as BooleanValue }
            "&&" -> castType<BooleanValue, BooleanValue>(node1.eval()) { a -> if (!a.value) BooleanValue(false) else node2.eval() as BooleanValue }

            else -> throw UnsupportedOperationException()
        }
    }

    fun UnaryOpNode.eval(): RuntimeValue {
        val result = node!!.eval()
        if (operator == "!!") {
            if (result == NullValue) {
                throw EvaluateNullPointerException()
            }
            return result as RuntimeValue
        }
        return when (result) {
            is NumberValue<*> -> when (operator) {
                "+" -> IntValue(0) + result
                "-" -> IntValue(0) - result
                "pre++" -> {
                    val newValue = result + IntValue(1)
                    node!!.write(newValue)
                    newValue
                }
                "pre--" -> {
                    val newValue = result - IntValue(1)
                    node!!.write(newValue)
                    newValue
                }
                "post++" -> {
                    val newValue = result + IntValue(1)
                    node!!.write(newValue)
                    result
                }
                "post--" -> {
                    val newValue = result - IntValue(1)
                    node!!.write(newValue)
                    result
                }
                else -> throw UnsupportedOperationException()
            }

            is BooleanValue -> {
                when (operator) {
                    "!" -> BooleanValue(!result.value)
                    else -> throw UnsupportedOperationException()
                }
            }
            else -> TODO()
        }
    }

    fun PropertyDeclarationNode.eval() {
        val symbolTable = callStack.currentSymbolTable()
        val name = transformedRefName!!
        if (initialValue != null) {
            val value = initialValue.eval()
            symbolTable.declareProperty(name, type, isMutable)
            symbolTable.assign(name, value as RuntimeValue)
        } else {
            symbolTable.declareProperty(name, type, isMutable)
        }
    }

    protected fun ASTNode.write(value: RuntimeValue) {
        when (this) {
            is VariableReferenceNode -> {
                if (this.ownerRef != null) {
                    NavigationNode(
                        subject = VariableReferenceNode(ownerRef!!.ownerRefName),
                        operator = ".",
                        member = ClassMemberReferenceNode(this.variableName, this.transformedRefName),
                        transformedRefName = ownerRef!!.extensionPropertyRef
                    ).write(value)
                } else {
                    callStack.currentSymbolTable().assign(this.transformedRefName ?: this.variableName, value)
                }
            }

            is NavigationNode -> {
                val subject = this.subject.eval() as RuntimeValue

                if (transformedRefName != null) { // extension property
                    (symbolTable().findExtensionProperty(transformedRefName!!)?.setter
                        ?: throw RuntimeException("Setter not found"))(subject, value)
                    return
                }

                val obj = subject as ClassInstance
//                    obj.assign((subject.member as ClassMemberReferenceNode).transformedRefName!!, value)
                // before type resolution is implemented in SemanticAnalyzer, reflect from clazz as a slower alternative
                obj.assign(interpreter = this@Interpreter, name = obj.clazz!!.memberPropertyNameToTransformedName[(this.member as ClassMemberReferenceNode).name]!!, value = value)/*?.also {
                    FunctionCallNode(
                        it,
                        listOf(FunctionCallArgumentNode(index = 0, value = ValueNode(value))),
                        SourcePosition(1, 1)
                    ).evalClassMemberAnyFunctionCall(obj, it)
                } // TODO remove */
            }

            else -> throw UnsupportedOperationException()
        }
    }

    fun AssignmentNode.eval() {
        if (subject is NavigationNode && subject.operator == "?.") {
            throw UnsupportedOperationException("?: on left side of assignment is not supported")
        }
        val result = value.eval() as RuntimeValue

        val read = { subject.eval() as RuntimeValue }
        val write = { value: RuntimeValue -> subject.write(value) }

        val finalResult = if (operator == "=") {
            result as RuntimeValue
        } else {
            val existing = read()
            val newResult = when (operator) {
                "+=" -> {
                    if (subject.declaredType() is StringType) {
                        StringValue(existing.convertToString() + result.convertToString())
                    }  else {
                        (existing as NumberValue<*>) + result as NumberValue<*>
                    }
                }
                "-=" -> (existing as NumberValue<*>) - result as NumberValue<*>
                "*=" -> (existing as NumberValue<*>) * result as NumberValue<*>
                "/=" -> (existing as NumberValue<*>) / result as NumberValue<*>
                "%=" -> (existing as NumberValue<*>) % result as NumberValue<*>
                else -> throw UnsupportedOperationException()
            }
            newResult
        }
        write(finalResult)
    }

    fun VariableReferenceNode.eval(): RuntimeValue {
        // usual variable -> transformedRefName
        // class constructor -> variableName? TODO
        if (ownerRef != null) {
            return NavigationNode(
                subject = VariableReferenceNode(ownerRef!!.ownerRefName),
                operator = ".",
                member = ClassMemberReferenceNode(variableName, transformedRefName),
                transformedRefName = ownerRef!!.extensionPropertyRef,
            ).eval()
        }
        if (type is ClassTypeNode) {
            // TODO return singleton
            val companionClassName = "${(type as ClassTypeNode).clazz.name}.Companion"
            return ClassInstance(
                companionClassName,
                symbolTable().findClass(companionClassName)!!.first,
                emptyList(),
            )
        }
        return callStack.currentSymbolTable().read(transformedRefName ?: variableName)
    }

    fun FunctionDeclarationNode.eval() {
        if (receiver == null) {
            callStack.currentSymbolTable().declareFunction(transformedRefName!!, this)
        } else {
            globalScope.declareExtensionFunction(transformedRefName!!, this)
        }
    }

    fun FunctionCallNode.eval(): RuntimeValue {
        // TODO move to semantic analyzer
        when (function) {
            is VariableReferenceNode -> {
                val directName = function.variableName

                if (this.function.ownerRef != null) {
                    return this.copy(
                        function = NavigationNode(
                            subject = VariableReferenceNode(this.function.ownerRef!!.ownerRefName),
                            operator = ".",
                            member = ClassMemberReferenceNode(directName),
                            transformedRefName = this.function.ownerRef!!.extensionPropertyRef,
                        )
                    ).eval()
                }

                when (callableType) {
                    CallableType.Function -> {
                        val functionNode = callStack.currentSymbolTable().findFunction(functionRefName!!)?.first
                        if (functionNode != null) {
                            return evalFunctionCall(functionNode)
                        }
                    }
                    CallableType.Constructor -> {
                        val classDefinition = callStack.currentSymbolTable().findClass(functionRefName!!)?.first
                        if (classDefinition != null) {
                            return evalCreateClassInstance(classDefinition, typeArguments)
                        }
                    }
                    CallableType.Property -> {
                        val variable = callStack.currentSymbolTable().read(functionRefName!!)
                        if (variable is LambdaValue) {
                            return evalFunctionCall(variable.value, extraSymbols = variable.symbolRefs)
                        }
                    }
                    else -> {}
                }
                throw RuntimeException("Function `$directName` not found")
            }

            is NavigationNode -> {
                val subject = function.subject.eval()
                if (subject == NullValue) {
                    if (function.operator == "?.") {
                        return NullValue // TODO not always true for extension functions
                    } else {
                        // extension methods of nullable types are allowed to be called
                    }
                }
                when (callableType) {
                    CallableType.ClassMemberFunction -> {
                        if (subject == NullValue) {
                            throw EvaluateNullPointerException()
                        }
                        (subject as? ClassInstance)?.clazz?.memberFunctions?.get(functionRefName)?.let { function ->
                            return evalClassMemberAnyFunctionCall(subject, function)
                        }
                    }
                    CallableType.ExtensionFunction -> {
                        val function = callStack.currentSymbolTable().findExtensionFunction(functionRefName!!)
                            ?: throw RuntimeException("Analysed function $functionRefName not found")
                        if (subject == NullValue && !function.receiver!!.isNullable) {
                            throw EvaluateNullPointerException()
                        }
                        return evalClassMemberAnyFunctionCall(subject as RuntimeValue, function)
                    }
                    else -> {}
                }
                throw RuntimeException("Class Function `${function.member.name}` not found")
            }

            else -> {
                val variable = function.eval() as? RuntimeValue
                if (variable is LambdaValue) {
                    return evalFunctionCall(variable.value, extraSymbols = variable.symbolRefs)
                } else {
                    throw RuntimeException("${variable?.type()} is not callable")
                }
            }
        }
    }

    fun FunctionCallNode.evalFunctionCall(functionNode: CallableNode, extraSymbols: SymbolTable? = null): RuntimeValue {
        return evalFunctionCall(this, functionNode, emptyMap(), emptyList(), extraSymbols).result
    }

    fun evalFunctionCall(callNode: FunctionCallNode, functionNode: CallableNode, extraScopeParameters: Map<String, RuntimeValue>, extraTypeResolutions: List<TypeParameterNode>, extraSymbols: SymbolTable? = null, subject: RuntimeValue? = null): FunctionCallResult {
        // TODO optimize to remove most loops
        val isVararg = functionNode.valueParameters.firstOrNull()?.modifiers?.contains(FunctionValueParameterModifier.vararg) ?: false
        val callArguments = if (isVararg) {
            callNode.arguments.map { a -> a.value.eval() as RuntimeValue? }.toTypedArray()
        } else {
            val callArguments = arrayOfNulls<RuntimeValue>(functionNode.valueParameters.size)
            callNode.arguments.forEach { a ->
                val index = if (a.name != null) {
                    functionNode.valueParameters.indexOfFirst { it.name == a.name }
                } else {
                    a.index
                }
                if (index < 0) throw RuntimeException("Named argument `${a.name}` could not be found.")
                callArguments[index] = a.value.eval() as RuntimeValue
            }
            callArguments
        }

        return evalFunctionCall(callArguments, callNode.typeArguments.toTypedArray(), callNode.position, functionNode, extraScopeParameters, extraTypeResolutions, extraSymbols, subject)
    }

    fun evalFunctionCall(
        arguments: Array<RuntimeValue?>,
        typeArguments: Array<TypeNode>,
        callPosition: SourcePosition,
        functionNode: CallableNode,
        extraScopeParameters: Map<String, RuntimeValue>,
        extraTypeResolutions: List<TypeParameterNode>,
        extraSymbols: SymbolTable? = null,
        subject: RuntimeValue? = null
    ): FunctionCallResult {
        val isVararg = functionNode.valueParameters.firstOrNull()?.modifiers?.contains(FunctionValueParameterModifier.vararg) ?: false
        if (!isVararg && arguments.size != functionNode.valueParameters.size) {
            throw RuntimeException("Arguments size not match. Optional arguments should be specified as null.")
        }

        if (!isVararg) {
            arguments.forEachIndexed { index, it ->
                val parameterNode = functionNode.valueParameters[index]
                if (it == null && parameterNode.defaultValue == null) {
                    throw RuntimeException("Missing parameter `${parameterNode.name} in function call ${functionNode.name}`")
                }
            }
        }

        val typeParametersReplacedWithArguments = (functionNode.typeParameters.mapIndexed { index, tp ->
            TypeParameterNode(tp.name, typeArguments[index])
        } + extraTypeResolutions) // add `extraTypeResolutions` at last because class type arguments have a lower precedence
            .associate { it.name to it.typeUpperBound!! }

        // resolve type arguments to DataType first, so that
        // class with same name of function type parameter name is resolved before function type parameter declarations
        val typeArgumentsInDataType = typeParametersReplacedWithArguments.mapValues {
            symbolTable().assertToDataType(it.value)
        }

        val scopeType = if (functionNode is FunctionDeclarationNode) ScopeType.Function else ScopeType.Closure
        val returnType = callStack.currentSymbolTable().typeNodeToPropertyType(
            type = functionNode.returnType.resolveGenericParameterTypeArguments(
//                (extraSymbols?.listTypeAliasInThisScope() ?: emptyList()) +
                typeParametersReplacedWithArguments + // typeParametersReplacedWithArguments has higher precedence
                (extraSymbols?.listTypeAliasResolutionInThisScope() ?: emptyMap()).mapValues {
                    it.value.toTypeNode()
                }
            ),
            isMutable = false
        )!!.type

        callStack.push(
            functionFullQualifiedName = functionNode.name,
            scopeType = scopeType,
            callPosition = callPosition,
        )
        try {
            val symbolTable = callStack.currentSymbolTable()
            extraSymbols?.let{
                symbolTable.mergeFrom(it)
            }
            extraScopeParameters.forEach {
                symbolTable.declareProperty(it.key, TypeNode(it.value.type().name, null, false), false) // TODO change to use DataType directly
                symbolTable.assign(it.key, it.value)
            }
//            typeParametersReplacedWithArguments.forEach {
//                symbolTable.declareTypeAlias(it.key, it.value)
//            }
            functionNode.typeParameters.forEach {
                symbolTable.declareTypeAlias(it.name, it.typeUpperBound)
            }
            typeParametersReplacedWithArguments.forEach {
                if (symbolTable.findTypeAlias(it.key) == null) {
                    symbolTable.declareTypeAlias(it.key, it.value) // TODO declare the original upper bound
                }
                symbolTable.declareTypeAliasResolution(it.key, typeArgumentsInDataType[it.key]!!)
            }
            functionNode.valueParameters.forEachIndexed { index, it ->
                if (!isVararg && !(functionNode is LambdaLiteralNode && it.name == "_")) {
                    val argumentType = it.type //.resolveGenericParameterTypeArguments(typeParametersReplacedWithArguments)
                    symbolTable.declareProperty(it.transformedRefName!!, argumentType, false)
                    symbolTable.assign(
                        it.transformedRefName!!,
                        arguments[index] ?: (it.defaultValue!!.eval() as RuntimeValue).also { arguments[index] = it }
                    )
                }
            }

            val arguments = if (isVararg) {
                arrayOf(ListValue(arguments.filterNotNull().toList(), symbolTable().assertToDataType(functionNode.valueParameters.first().type)))
            } else {
                arguments
            }

            // execute function
            val returnValue = try {
                val result = functionNode.execute(this, subject, arguments.toList() as List<RuntimeValue>, typeArgumentsInDataType.toMap())
                if (returnType is UnitType) {
                    UnitValue
                } else {
                    result
                }
            } catch (r: NormalReturnException) {
                r.value
            }

            log.v { "Fun Return $returnValue; symbolTable = $symbolTable" }
            if (!returnType.isAssignableFrom(returnValue.type())) {
                throw RuntimeException("Type ${returnValue.type().descriptiveName} cannot be casted to ${returnType.descriptiveName}")
            }

            return FunctionCallResult(returnValue, symbolTable)
        } finally {
            callStack.pop(scopeType)
        }
    }

    fun FunctionCallNode.evalCreateClassInstance(clazz: ClassDefinition, typeArguments: List<TypeNode>): ClassInstance {
        callStack.push(functionFullQualifiedName = "class", scopeType = ScopeType.ClassInitializer, callPosition = this.position)
        try {
            // TODO generalize duplicated code
            val parameters = clazz.primaryConstructor?.parameters ?: emptyList()
            val callArguments = arrayOfNulls<RuntimeValue>(parameters.size)
            arguments.forEach { a ->
                val index = if (a.name != null) {
                    parameters.indexOfFirst { it.parameter.name == a.name }
                } else {
                    a.index
                }
                if (index < 0) throw RuntimeException("Named argument `${a.name}` could not be found.")
                callArguments[index] = a.value.eval() as RuntimeValue
            }
            callArguments.forEachIndexed { index, it ->
                val parameterNode = parameters[index].parameter
                if (it == null && parameterNode.defaultValue == null) {
                    throw RuntimeException("Missing parameter `${parameterNode.name} in constructor call of ${clazz.name}`")
                }
            }

            val typeArgumentByName = clazz.typeParameters.mapIndexed { index, tp ->
                tp.name to typeArguments[index]
            }.toMap()

            val symbolTable = callStack.currentSymbolTable()
            clazz.primaryConstructor?.parameters?.forEachIndexed { index, it ->
                // no need to use transformedRefName as duplicated declarations are not possible here
                val value = callArguments[index] ?: (it.parameter.defaultValue!!.eval() as RuntimeValue)
                symbolTable.declareProperty(it.parameter.transformedRefName!!, it.parameter.type.resolveGenericParameterTypeArguments(typeArgumentByName), false)
                symbolTable.assign(it.parameter.transformedRefName!!, value)
                callArguments[index] = value
            }

            return clazz.construct(this@Interpreter, callArguments as Array<RuntimeValue>, typeArguments.map { symbolTable().typeNodeToDataType(it)!! }.toTypedArray(), position)
        } finally {
            callStack.pop(ScopeType.ClassInitializer)
        }
    }

    fun constructClassInstance(callArguments: Array<RuntimeValue>, callPosition: SourcePosition, typeArguments: Array<DataType>, clazz: ClassDefinition): ClassInstance {
        val instance = ClassInstance(clazz.fullQualifiedName, clazz, typeArguments.toList())
        val properties = clazz.primaryConstructor?.parameters?.filter { it.isProperty }?.map { it.parameter.transformedRefName!! }?.toMutableSet() ?: mutableSetOf()

        val symbolTable = callStack.currentSymbolTable()
        val nonPropertyArguments = mutableMapOf<String, Pair<TypeNode, RuntimeValue>>()
        clazz.primaryConstructor?.parameters?.forEachIndexed { index, it ->
            val value = callArguments[index]
            if (it.isProperty) {
                instance.assign(name = it.parameter.transformedRefName!!, value = value)
//                    instance.memberPropertyValues[it.parameter.transformedRefName!!] = value
            } else {
                nonPropertyArguments[it.parameter.transformedRefName!!] = Pair(it.parameter.type, value)
            }
        }

        // move nonPropertyArguments from outer scope into inner scope
        nonPropertyArguments.keys.forEach {
            symbolTable.undeclareProperty(it)
        }
//            clazz.primaryConstructor?.parameters?.forEach {
//                symbolTable.undeclareProperty(it.parameter.transformedRefName!!)
//            }

        // variable "this" is available after primary constructor
        symbolTable.declareProperty("this", TypeNode(instance.clazz!!.name, typeArguments.map { it.toTypeNode() }.emptyToNull(), false), false)
        symbolTable.assign("this", instance)
        symbolTable.declareProperty("this/${instance.clazz!!.fullQualifiedName}", TypeNode(instance.clazz!!.name, typeArguments.map { it.toTypeNode() }.emptyToNull(), false), false)
        symbolTable.assign("this/${instance.clazz!!.fullQualifiedName}", instance)
        symbolTable.registerTransformedSymbol(IdentifierClassifier.Property, "this", "this")
        symbolTable.registerTransformedSymbol(IdentifierClassifier.Property, "this/${instance.clazz!!.fullQualifiedName}", "this")
//            instance.memberPropertyValues.forEach {
//                symbolTable.putPropertyHolder(instance.clazz!!.memberPropertyNameToTransformedName[it.key]!!, it.value)
//            }

        val typeParametersAndArguments = clazz.typeParameters.mapIndexed { index, tp ->
            TypeParameterNode(tp.name, typeArguments[index].toTypeNode())
        }
        val typeArgumentByName = typeParametersAndArguments.associate {
            it.name to it.typeUpperBound!!
        }

        clazz!!.orderedInitializersAndPropertyDeclarations.forEach {
            callStack.push(
                functionFullQualifiedName = "init-property",
                scopeType = ScopeType.ClassInitializer,
                callPosition = callPosition
            )
            try {
                val innerSymbolTable = callStack.currentSymbolTable()
                nonPropertyArguments.forEach {
                    val keyWithIncreasedScope = it.key.replaceAfterLast("/", innerSymbolTable.scopeLevel.toString())
                    log.v("keyWithIncreasedScope = $keyWithIncreasedScope")
                    innerSymbolTable.declareProperty(keyWithIncreasedScope, it.value.first.resolveGenericParameterTypeArguments(typeArgumentByName), false)
                    innerSymbolTable.assign(keyWithIncreasedScope, it.value.second)
                }
                when (it) {
                    is PropertyDeclarationNode -> {
                        properties += it.transformedRefName!!
                        val value = it.initialValue?.eval() as RuntimeValue?
                        value?.let { value ->
                            instance.assign(name = it.transformedRefName!!, value = value)
                        }
                    }

                    is ClassInstanceInitializerNode -> {
                        val init = FunctionDeclarationNode(
                            name = "init",
                            declaredReturnType = TypeNode("Unit", null, false),
                            valueParameters = emptyList(),
                            body = it.block
                        )
                        evalFunctionCall(
                            callNode = FunctionCallNode(
                                function = init, /* not used */
                                arguments = emptyList(),
                                declaredTypeArguments = emptyList(),
                                position = callPosition,
                            ),
                            functionNode = init,
                            extraScopeParameters = emptyMap(),
                            extraTypeResolutions = typeParametersAndArguments /* type arguments */,
                        )
                    }

                    else -> Unit
                }
            } finally {
                callStack.pop(ScopeType.ClassInitializer)
            }
        }
        return instance
    }

//    fun FunctionCallNode.evalClassMemberFunctionCall(subject: ClassInstance, member: ClassMemberReferenceNode): RuntimeValue {
//        val function = subject.clazz!!.memberFunctions[member.name] ?: throw EvaluateRuntimeException("Member function `${member.name}` not found")
//        return evalClassMemberAnyFunctionCall(subject, function)
//    }

    fun FunctionCallNode.evalClassMemberAnyFunctionCall(subject: RuntimeValue, function: FunctionDeclarationNode): RuntimeValue {
        callStack.push(functionFullQualifiedName = "class", scopeType = ScopeType.ClassMemberFunction, callPosition = this.position)
        try {
            val symbolTable = callStack.currentSymbolTable()
            symbolTable.declareProperty("this/${subject.type().name}", subject.type().toTypeNode(), false)
            symbolTable.assign("this/${subject.type().name}", subject)
            if (function.receiver != null && function.receiver!!.descriptiveName() != subject.type().name) {
                symbolTable.declareProperty("this/${function.receiver!!.descriptiveName()}", subject.type().toTypeNode(), false)
                symbolTable.assign("this/${function.receiver!!.descriptiveName()}", subject)
            }
            symbolTable.declareProperty("this", subject.type().toTypeNode(), false)
            symbolTable.assign("this", subject)
            symbolTable.registerTransformedSymbol(IdentifierClassifier.Property, "this", "this")
            symbolTable.registerTransformedSymbol(IdentifierClassifier.Property, "this/${subject.type().name}", "this")

//            // TODO optimize to only copy needed members
//            if (subject is ClassInstance) {
//                subject.memberPropertyValues.forEach {
//                    symbolTable.putPropertyHolder(subject.clazz!!.memberPropertyNameToTransformedName[it.key]!!, it.value)
//                }
//            }

            val instanceGenericTypeResolutions = if (subject is ClassInstance) {
                subject.clazz!!.typeParameters.mapIndexed { index, it ->
                    TypeParameterNode(it.name, subject.typeArguments[index].toTypeNode())
                }
            } else emptyList()

            val result = evalFunctionCall(
                callNode = this.copy(function = function),
                functionNode = function,
                extraScopeParameters = emptyMap(),
                extraTypeResolutions = instanceGenericTypeResolutions /* type arguments */,
                subject = subject,
            )

            return result.result
        } finally {
            callStack.pop(ScopeType.ClassMemberFunction)
        }
    }

    fun BlockNode.eval(): RuntimeValue {
        // additional scope because new variables can be declared in blocks of `if`, `while`, etc.
        // also, function parameters can be shadowed
        callStack.push(functionFullQualifiedName = null, scopeType = type, callPosition = position)
        val result = try {
            statements.map { it.eval() as? RuntimeValue }.lastOrNull() ?: UnitValue
        } finally {
            callStack.pop(type)
        }
        return result
    }

    fun ReturnNode.eval() {
        val value = (value?.eval() ?: UnitValue) as RuntimeValue
        throw NormalReturnException(returnToAddress, value)
    }

    fun BreakNode.eval() {
        throw NormalBreakException()
    }

    fun ContinueNode.eval() {
        throw NormalContinueException()
    }

    fun IfNode.eval(): RuntimeValue {
        val conditionalValue = condition.eval() as BooleanValue
        return if (conditionalValue.value) {
            trueBlock?.eval() ?: UnitValue
        } else {
            falseBlock?.eval() ?: UnitValue
        }
    }

    fun WhileNode.eval() {
//        if (conditionalValue.value) {
//            if (body == null || body.statements.isEmpty()) {
//                throw NotPermittedOperationException("Infinite loop is not allowed")
//            }
//        }
        // TODO detect infinite loop
        try {
            while ((condition.eval() as BooleanValue).value) {
                try {
                    body?.eval()
                } catch (_: NormalContinueException) {}
            }
        } catch (_: NormalBreakException) {}
    }

    fun ClassDeclarationNode.eval() {
        val declarationScope = callStack.currentSymbolTable()
        val classType = TypeNode(
            name = fullQualifiedName,
            arguments = typeParameters.map { TypeNode(it.name, null, false) }.emptyToNull(),
            isNullable = false,
        )

        callStack.push(fullQualifiedName, ScopeType.Class, SourcePosition(1, 1))
        try {
            typeParameters.forEach {
                callStack.currentSymbolTable().declareTypeAlias(it.name, it.typeUpperBound)
            }

            declarationScope.declareClass(ClassDefinition(
                currentScope = callStack.currentSymbolTable(),
                name = name,
                fullQualifiedName = fullQualifiedName,
                typeParameters = typeParameters,
                isInstanceCreationAllowed = true,
                primaryConstructor = primaryConstructor,
                rawMemberProperties = ((primaryConstructor?.parameters
                    ?.filter { it.isProperty }
                    ?.map {
                        val p = it.parameter
                        PropertyDeclarationNode(
                            name = p.name,
                            typeParameters = emptyList(),
                            receiver = classType,
                            declaredType = p.type,
                            isMutable = it.isMutable,
                            initialValue = p.defaultValue,
                            transformedRefName = p.transformedRefName,
                        )
                    } ?: emptyList()) +
                        declarations.filterIsInstance<PropertyDeclarationNode>()),
                memberFunctions = declarations
                    .filterIsInstance<FunctionDeclarationNode>()
                    .filter { it.receiver == null }
                    .associateBy { it.transformedRefName!! },
                orderedInitializersAndPropertyDeclarations = declarations
                    .filter { it is ClassInstanceInitializerNode || it is PropertyDeclarationNode },
            ))
            // register extension functions in global scope
            declarations
                .filterIsInstance<FunctionDeclarationNode>()
                .filter { it.receiver != null }
                .forEach { globalScope.declareExtensionFunction(it.transformedRefName!!, it) }
        } finally {
            callStack.pop(ScopeType.Class)
        }

        // companion object
        // TODO create only if a companion object is declared
        callStack.currentSymbolTable().declareClass(ClassDefinition(
            currentScope = callStack.currentSymbolTable(),
            name = "$name.Companion",
            fullQualifiedName = "$fullQualifiedName.Companion",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = false,
            orderedInitializersAndPropertyDeclarations = emptyList(),
            rawMemberProperties = emptyList(),
            memberFunctions = emptyMap(),
            primaryConstructor = null
        ))
    }

    fun NavigationNode.eval(): RuntimeValue {
        val obj = subject.eval() as RuntimeValue
//        return obj.memberPropertyValues[member.transformedRefName!!]!!

        if (transformedRefName != null) {
            val extensionProperty = symbolTable().findExtensionProperty(transformedRefName!!)
                ?: throw RuntimeException("Extension property `${member.name}` on receiver `${obj.type().nameWithNullable}` could not be found")
            extensionProperty.getter?.let { getter ->
                return getter(obj)
            }
        }

        if (obj == NullValue) throw EvaluateNullPointerException()
        obj as? ClassInstance ?: throw RuntimeException("Cannot access member `${member.name}` for type `${obj.type().nameWithNullable}`")
        // before type resolution is implemented in SemanticAnalyzer, reflect from clazz as a slower alternative
        return when (val r = obj.read(interpreter = this@Interpreter, name = obj.clazz!!.memberPropertyNameToTransformedName[member.name]!!)) {
            is RuntimeValue -> r
            /*is FunctionDeclarationNode -> {
                FunctionCallNode(
                    function = r,
                    arguments = emptyList(),
                    position = SourcePosition(1, 1)
                ).evalClassMemberAnyFunctionCall(obj, r)
            } // TODO remove */
            else -> throw UnsupportedOperationException()
        }
    }

    fun AsOpNode.eval(): RuntimeValue {
        val value = expression.eval() as RuntimeValue
        val targetType = symbolTable().typeNodeToDataType(type) ?: throw RuntimeException("Unknown type `$type`")
        return if (value.type().isAssignableTo(targetType)) {
            value
        } else if (isNullable) {
            NullValue
        } else {
            throw EvaluateTypeCastException(value.type().nameWithNullable, targetType.nameWithNullable)
        }
    }

    fun LambdaLiteralNode.eval(): RuntimeValue {
        val refs = this.accessedRefs!!
        val currentSymbolTable = callStack.currentSymbolTable()
        val runtimeRefs = SymbolTable(Int.MAX_VALUE, "lambda-symbol-ref", ScopeType.Closure, null)
        refs.properties.forEach {
            runtimeRefs.putPropertyHolder(it, currentSymbolTable.getPropertyHolder(it))
        }
        refs.functions.forEach {
            runtimeRefs.declareFunction(it, currentSymbolTable.findFunction(it)!!.first)
        }
        refs.classes.forEach {
            runtimeRefs.declareClass(currentSymbolTable.findClass(it)!!.first)
        }
        refs.typeAlias.forEach {
//            runtimeRefs.declareTypeAlias(it, currentSymbolTable.findTypeAlias(it)!!.first.toTypeNode(), currentSymbolTable)
            val resolution = currentSymbolTable.findTypeAliasResolution(it)!!.toTypeNode()
            runtimeRefs.declareTypeAlias(it, currentSymbolTable.findTypeAlias(it)!!.first.toTypeNode(), currentSymbolTable)
            runtimeRefs.declareTypeAliasResolution(it, resolution, currentSymbolTable)
        }

//        fun processTypeParameter(dataType: DataType) {
//            when (dataType) {
//                is TypeParameterType -> {
//                    runtimeRefs.declareTypeAlias(dataType.name, dataType.upperBound.toTypeNode())
//                }
//                is FunctionType -> {
//                    dataType.arguments.forEach { processTypeParameter(it) }
//                    processTypeParameter(dataType.returnType)
//                }
//                is ObjectType -> {
//                    dataType.arguments.forEach { processTypeParameter(it) }
//                }
//                else -> {}
//            }
//        }
        val lambdaType = callStack.currentSymbolTable().typeNodeToDataType(type!!) as FunctionType
//        processTypeParameter(lambdaType)

        return LambdaValue(this, lambdaType, runtimeRefs, this@Interpreter)
    }

    fun StringNode.eval(): StringValue {
        return StringValue(nodes.joinToString("") { (it.eval() as RuntimeValue).convertToString() })
    }

    fun StringLiteralNode.eval() = StringValue(content)

    fun IntegerNode.eval() = IntValue(value)
    fun LongNode.eval() = LongValue(value)
    fun DoubleNode.eval() = DoubleValue(value)
    fun BooleanNode.eval() = BooleanValue(value)
    fun CharNode.eval() = CharValue(value)
    fun NullNode.eval() = NullValue
    fun ValueNode.eval() = value

    fun ASTNode.declaredType(): DataType = when (this) {
        is NavigationNode -> this.declaredType()
        is VariableReferenceNode -> this.declaredType()
        else -> throw UnsupportedOperationException()
    }


    fun NavigationNode.declaredType(): DataType {
        return callStack.currentSymbolTable().typeNodeToPropertyType(type!!, false)!!.type
    }

    fun VariableReferenceNode.declaredType(): DataType {
        return callStack.currentSymbolTable().typeNodeToPropertyType(type!!, false)!!.type
    }

    fun eval() = scriptNode.eval()

}
