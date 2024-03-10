package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateNullPointerException
import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateRuntimeException
import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateTypeCastException
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalBreakException
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalContinueException
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalReturnException
import com.sunnychung.lib.multiplatform.kotlite.extension.emptyToNull
import com.sunnychung.lib.multiplatform.kotlite.extension.fullClassName
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
import com.sunnychung.lib.multiplatform.kotlite.model.CatchNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharValue
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassModifier
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.ComparableRuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ElvisOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.EnumEntryNode
import com.sunnychung.lib.multiplatform.kotlite.model.ExceptionValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.ForNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallResult
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionType
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IndexOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.InfixFunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.LabelNode
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
import com.sunnychung.lib.multiplatform.kotlite.model.PairValue
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
import com.sunnychung.lib.multiplatform.kotlite.model.ThrowNode
import com.sunnychung.lib.multiplatform.kotlite.model.ThrowableValue
import com.sunnychung.lib.multiplatform.kotlite.model.TryNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue
import com.sunnychung.lib.multiplatform.kotlite.model.ValueNode
import com.sunnychung.lib.multiplatform.kotlite.model.ValueParameterDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenConditionNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenEntryNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenSubjectNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode
import com.sunnychung.lib.multiplatform.kotlite.util.ClassMemberResolver

class Interpreter(val scriptNode: ScriptNode, val executionEnvironment: ExecutionEnvironment) {

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
            is IndexOpNode -> this.eval()
            is InfixFunctionCallNode -> this.eval()
            is ElvisOpNode -> this.eval()
            is ThrowNode -> this.eval()
            is CatchNode -> TODO()
            is TryNode -> this.eval()
            is WhenConditionNode -> TODO()
            is WhenEntryNode -> TODO()
            is WhenNode -> this.eval()
            is WhenSubjectNode -> TODO()
            is LabelNode -> TODO()
            is EnumEntryNode -> TODO()
            is ForNode -> this.eval()
            is ValueParameterDeclarationNode -> TODO()
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
                throw EvaluateNullPointerException(callStack.currentSymbolTable(), callStack.getStacktrace(position))
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
            symbolTable.declareProperty(position, name, type, isMutable)
            symbolTable.assign(name, value as RuntimeValue)
        } else {
            symbolTable.declareProperty(position, name, type, isMutable)
        }
    }

    protected fun ASTNode.write(value: RuntimeValue) {
        when (this) {
            is VariableReferenceNode -> {
                if (this.ownerRef != null) {
                    NavigationNode(
                        position = position,
                        subject = VariableReferenceNode(position = position, variableName = ownerRef!!.ownerRefName),
                        operator = ".",
                        member = ClassMemberReferenceNode(
                            position = this.position,
                            name = this.variableName,
                            transformedRefName = this.transformedRefName
                        ),
                        memberType = NavigationNode.MemberType.Extension,
                        transformedRefName = ownerRef!!.extensionPropertyRef
                    ).write(value)
                } else {
                    callStack.currentSymbolTable().assign(this.transformedRefName ?: this.variableName, value)
                }
            }

            is NavigationNode -> {
                val subject = (this.subject.eval() as RuntimeValue)
                    .let { resolveSuperKeyword(it) }

                if (transformedRefName != null) { // extension property
                    val extensionProperty = symbolTable().findExtensionProperty(transformedRefName!!) ?: throw RuntimeException("Extension property `$transformedRefName` not found")
                    val typeArgumentsMap = extensionProperty.typeArgumentsMap(subject.type())
                    (extensionProperty.setter ?: throw RuntimeException("Setter not found"))(
                        this@Interpreter,
                        subject,
                        value,
                        typeArgumentsMap,
                    )
                    return
                }

                val obj = subject as ClassInstance
//                    obj.assign((subject.member as ClassMemberReferenceNode).transformedRefName!!, value)
                // before type resolution is implemented in SemanticAnalyzer, reflect from clazz as a slower alternative
                obj.assign(interpreter = this@Interpreter, name = obj.clazz!!.findMemberPropertyTransformedName((this.member as ClassMemberReferenceNode).name)!!, value = value)/*?.also {
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

    fun NavigationNode.resolveSuperKeyword(subjectValue: RuntimeValue): RuntimeValue {
        // a hack to resolve the "super" keyword. See documentation
        return if (subject is VariableReferenceNode && subject.variableName == "super" && (subjectValue as ClassInstance).parentInstance != null) {
            var instance: ClassInstance? = subjectValue as ClassInstance
            val typeOfSuper = subject.type!!
            while (instance != null && instance.clazz!!.fullQualifiedName != typeOfSuper.name) {
                instance = instance.parentInstance
            }
            instance!!
        } else {
            subjectValue
        }
    }

    fun AssignmentNode.eval() {
        if (subject is NavigationNode && subject.operator == "?.") {
            throw UnsupportedOperationException("?: on left side of assignment is not supported")
        }
        val result = value.eval() as RuntimeValue

        val read = { subject.eval() as RuntimeValue }
        val write = { value: RuntimeValue ->
            if (functionCall != null) {
                // TODO any less "hacky" way to implement?
                functionCall!!.eval(replaceArguments = mapOf(functionCall!!.arguments.lastIndex to value))
            } else {
                subject.write(value)
            }
        }

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
                position = position,
                subject = VariableReferenceNode(position = position, variableName = ownerRef!!.ownerRefName),
                operator = ".",
                member = ClassMemberReferenceNode(
                    position = position,
                    name = variableName,
                    transformedRefName = transformedRefName
                ),
                memberType = NavigationNode.MemberType.Extension,
                transformedRefName = ownerRef!!.extensionPropertyRef,
            ).eval()
        }
        if (type is ClassTypeNode) {
            // TODO return singleton
            val companionClassName = "${(type as ClassTypeNode).clazz.name}.Companion"
            return ClassInstance(
                symbolTable(),
                companionClassName,
                symbolTable().findClass(companionClassName)!!.first,
                emptyList(),
            )
        }
        return callStack.currentSymbolTable().read(transformedRefName ?: variableName)
    }

    fun FunctionDeclarationNode.eval() {
        if (receiver == null) {
            callStack.currentSymbolTable().declareFunction(position, transformedRefName!!, this)
        } else {
            globalScope.declareExtensionFunction(position, transformedRefName!!, this)
        }
    }

    fun FunctionCallNode.eval(replaceArguments: Map<Int, RuntimeValue> = emptyMap()): RuntimeValue {
        // TODO move to semantic analyzer
        when (function) {
            is VariableReferenceNode, is TypeNode -> {
                val directName = when (function) {
                    is VariableReferenceNode -> function.variableName
                    is TypeNode -> function.name
                    else -> throw UnsupportedOperationException()
                }

                if (function is VariableReferenceNode && function.ownerRef != null) {
                    return this.copy(
                        function = NavigationNode(
                            position = position,
                            subject = VariableReferenceNode(
                                position = position,
                                variableName = this.function.ownerRef!!.ownerRefName
                            ),
                            operator = ".",
                            member = ClassMemberReferenceNode(position = position, name = directName),
                            memberType = NavigationNode.MemberType.Extension,
                            transformedRefName = this.function.ownerRef!!.extensionPropertyRef,
                        )
                    ).eval()
                }

                when (callableType) {
                    CallableType.Function -> {
                        val functionNode = callStack.currentSymbolTable().findFunction(functionRefName!!)?.first
                        if (functionNode != null) {
                            return evalFunctionCall(functionNode, replaceArguments = replaceArguments)
                        }
                    }
                    CallableType.Constructor -> {
                        val classDefinition = callStack.currentSymbolTable().findClass(functionRefName!!)?.first
                        if (classDefinition != null) {
                            return evalCreateClassInstance(classDefinition, typeArguments, replaceArguments = replaceArguments)
                        }
                    }
                    CallableType.Property -> {
                        val variable = callStack.currentSymbolTable().read(functionRefName!!)
                        if (variable is LambdaValue) {
                            return evalFunctionCall(variable.value, extraSymbols = variable.symbolRefs, replaceArguments = replaceArguments)
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
                            throw EvaluateNullPointerException(callStack.currentSymbolTable(), callStack.getStacktrace(position))
                        }
                        (subject as? ClassInstance)
                            ?.let { function.resolveSuperKeyword(it) as? ClassInstance }
                            ?.clazz
                            ?.findMemberFunctionByTransformedName(functionRefName!!)
                            ?.let { function ->
                                return evalClassMemberAnyFunctionCall(subject, function, replaceArguments = replaceArguments)
                            }
                    }
                    CallableType.ExtensionFunction -> {
                        val function = callStack.currentSymbolTable().findExtensionFunction(functionRefName!!)
                            ?: throw RuntimeException("Analysed function $functionRefName not found")
                        if (subject == NullValue && !function.receiver!!.isNullable) {
                            throw EvaluateNullPointerException(callStack.currentSymbolTable(), callStack.getStacktrace(position))
                        }
                        return evalClassMemberAnyFunctionCall(subject as RuntimeValue, function, replaceArguments = replaceArguments)
                    }
                    else -> {}
                }
                throw RuntimeException("Class Function `${function.member.name}` not found")
            }

            else -> {
                val variable = function.eval() as? RuntimeValue
                if (variable is LambdaValue) {
                    return evalFunctionCall(variable.value, extraSymbols = variable.symbolRefs, replaceArguments = replaceArguments)
                } else {
                    throw RuntimeException("${variable?.type()} is not callable")
                }
            }
        }
    }

    fun FunctionCallNode.evalFunctionCall(functionNode: CallableNode, extraSymbols: SymbolTable? = null, replaceArguments: Map<Int, RuntimeValue> = emptyMap()): RuntimeValue {
        return evalFunctionCall(this, functionNode, emptyMap(), emptyList(), extraSymbols, replaceArguments).result
    }

    fun evalFunctionCall(
        callNode: FunctionCallNode,
        functionNode: CallableNode,
        extraScopeParameters: Map<String, RuntimeValue>,
        extraTypeResolutions: List<TypeParameterNode>,
        extraSymbols: SymbolTable? = null,
        replaceArguments: Map<Int, RuntimeValue> = emptyMap(),
        subject: RuntimeValue? = null,
    ): FunctionCallResult {
        // TODO optimize to remove most loops
        val isVararg =
            functionNode.valueParameters.firstOrNull()?.modifiers?.contains(FunctionValueParameterModifier.vararg)
                ?: false
        val callArguments = if (isVararg) {
            callNode.arguments.map { a -> a.value.eval() as RuntimeValue? }.toTypedArray()
        } else {
            val callArguments = arrayOfNulls<RuntimeValue>(functionNode.valueParameters.size)
            callNode.arguments.forEachIndexed { i, a ->
                val value = replaceArguments[i] ?: a.value.eval() as RuntimeValue
                val index = if (a.name != null) {
                    functionNode.valueParameters.indexOfFirst { it.name == a.name }
                } else if (i == callNode.arguments.lastIndex && value is LambdaValue) {
                    functionNode.valueParameters.lastIndex
                } else {
                    a.index
                }
                if (index < 0) throw RuntimeException("Named argument `${a.name}` could not be found.")
                callArguments[index] = value
            }
            callArguments
        }

        return evalFunctionCall(callArguments, callNode.typeArguments.toTypedArray(), callNode.position, functionNode, extraScopeParameters, extraTypeResolutions, extraSymbols, replaceArguments, subject)
    }

    fun evalFunctionCall(
        arguments: Array<RuntimeValue?>,
        typeArguments: Array<TypeNode>,
        callPosition: SourcePosition,
        functionNode: CallableNode,
        extraScopeParameters: Map<String, RuntimeValue>,
        extraTypeResolutions: List<TypeParameterNode>,
        extraSymbols: SymbolTable? = null,
        replaceArguments: Map<Int, RuntimeValue> = emptyMap(),
        subject: RuntimeValue? = null
    ): FunctionCallResult {
        val isVararg = functionNode.valueParameters.firstOrNull()?.modifiers?.contains(FunctionValueParameterModifier.vararg) ?: false
        if (!isVararg && arguments.size != functionNode.valueParameters.size) {
            throw RuntimeException("Arguments size not match. Optional arguments should be specified as null.")
        }

        if (!isVararg) {
            arguments.forEachIndexed { index, it ->
                val parameterNode = functionNode.valueParameters[index]
                if (replaceArguments[index] == null && it == null && parameterNode.defaultValue == null) {
                    throw RuntimeException("Missing parameter `${parameterNode.name} in function call ${functionNode.name}`")
                }
            }
        }

        val classResolver = (functionNode as? FunctionDeclarationNode)
            ?.takeIf { it.transformedRefName != null }
            ?.let { function ->
                val subjectType = subject?.type() as? ObjectType
                subjectType?.let { subjectType ->
                    ClassMemberResolver(symbolTable(), subjectType.clazz, subjectType.arguments.map { it.toTypeNode() })
                }
            }
        val resolvedFunction = (functionNode as? FunctionDeclarationNode)
            ?.takeIf { it.transformedRefName != null }
            ?.let { function ->
                classResolver?.findMemberFunctionWithTypeByTransformedName(function.transformedRefName!!)
            }

        val typeParametersReplacedWithArguments = (
                extraTypeResolutions + // add `extraTypeResolutions` at first because class type arguments have a lower precedence
                (classResolver?.let { resolver ->
                    resolvedFunction?.enclosingTypeName?.let { typeName ->
                        resolver.genericResolutionsByTypeName[typeName]!!.map {
                            TypeParameterNode(it.value.position, it.key, it.value)
                        }
                    }
                } ?: emptyList()) +
                functionNode.typeParameters.mapIndexed { index, tp ->
                    TypeParameterNode(tp.position, tp.name, typeArguments[index])
                }
            )
            .associate { it.name to it.typeUpperBound!! }

        // resolve type arguments to DataType first, so that
        // class with same name of function type parameter name is resolved before function type parameter declarations
        val typeArgumentsInDataType = typeParametersReplacedWithArguments.mapValues {
            symbolTable().assertToDataType(it.value)
        }

        val scopeType = if (functionNode is FunctionDeclarationNode) ScopeType.Function else ScopeType.Closure

        val returnType = callStack.currentSymbolTable().assertToDataType(
            // 2nd resolution is needed, because the generic type may not be relevant to the class itself.
            // see test case GenericFunctionAndExtensionFunctionWithGenericClassTest#unrelatedTypeParameter()
            type = (resolvedFunction?.resolvedReturnType ?: functionNode.returnType).resolveGenericParameterTypeArguments(
//                (extraSymbols?.listTypeAliasInThisScope() ?: emptyList()) +
                typeParametersReplacedWithArguments + // typeParametersReplacedWithArguments has higher precedence
                (extraSymbols?.listTypeAliasResolutionInThisScope() ?: emptyMap()).mapValues {
                    it.value.toTypeNode()
                }
            ),
        )

        callStack.push(
            functionFullQualifiedName = functionNode.name,
            isFunctionCall = true,
            scopeType = scopeType,
            callPosition = callPosition,
        )
        try {
            val symbolTable = callStack.currentSymbolTable()
            extraSymbols?.let{
                symbolTable.mergeFrom(callPosition, it)
            }
            extraScopeParameters.forEach {
                symbolTable.declareProperty(callPosition, it.key, TypeNode(callPosition, it.value.type().name, null, false), false) // TODO change to use DataType directly
                symbolTable.assign(it.key, it.value)
            }
            functionNode.typeParameters.forEach {
                symbolTable.declareTypeAlias(callPosition, it.name, it.typeUpperBound)
            }
            typeParametersReplacedWithArguments.forEach {
                if (symbolTable.findTypeAlias(it.key) == null) {
                    symbolTable.declareTypeAlias(callPosition, it.key, it.value) // TODO declare the original upper bound
                }
                symbolTable.declareTypeAliasResolution(callPosition, it.key, typeArgumentsInDataType[it.key]!!)
            }
            val valueParametersWithGenericsResolved = resolvedFunction?.resolvedValueParameterTypes
                ?: functionNode.valueParameters
            val varargListValueArgument = if (isVararg) {
                ListValue(arguments.filterNotNull().toList(), symbolTable().assertToDataType(functionNode.valueParameters.first().type), symbolTable())
            } else null
            functionNode.valueParameters.forEachIndexed { index, it ->
                if (!isVararg && !(functionNode is LambdaLiteralNode && it.name == "_")) {
                    val argumentType = valueParametersWithGenericsResolved[index].type
                    symbolTable.declareProperty(callPosition, it.transformedRefName!!, argumentType, false)
                    symbolTable.assign(
                        name = it.transformedRefName!!,
                        value = replaceArguments[index] ?: arguments[index] ?: (it.defaultValue!!.eval() as RuntimeValue)
                            .also { arguments[index] = it }
                    )
                } else if (isVararg) {
                    val argumentType = varargListValueArgument!!.type().toTypeNode()
                    symbolTable.declareProperty(callPosition, it.transformedRefName!!, argumentType, false)
                    symbolTable.assign(
                        name = it.transformedRefName!!,
                        value = varargListValueArgument,
                    )
                }
            }

            val arguments = if (isVararg) {
                arrayOf(varargListValueArgument)
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
                if (r.returnToLabel.isEmpty()) {
                    if (functionNode.labelName != null) {
                        throw RuntimeException("Returning to a non-function callable")
                    }
                } else {
                    if (functionNode.labelName == null) {
                        throw RuntimeException("Returning to a non-lambda callable")
                    } else if (functionNode.labelName != r.returnToLabel) {
                        throw RuntimeException("Returning to a lambda with mismatching label")
                    }
                }

                r.value
            }

            log.v { "Fun Return $returnValue; symbolTable = $symbolTable" }
            if (!returnType.isConvertibleFrom(returnValue.type())) {
                throw RuntimeException("Return value's type ${returnValue.type().descriptiveName} cannot be casted to ${returnType.descriptiveName} in function `${functionNode.name}`")
            }

            return FunctionCallResult(returnValue, symbolTable)
        } finally {
            callStack.pop(scopeType)
        }
    }

    fun FunctionCallNode.evalCreateClassInstance(clazz: ClassDefinition, typeArguments: List<TypeNode>, replaceArguments: Map<Int, RuntimeValue> = emptyMap()): ClassInstance {
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
                callArguments[index] = replaceArguments[index] ?: a.value.eval() as RuntimeValue
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
                symbolTable.declareProperty(it.position, it.parameter.transformedRefName!!, it.parameter.type.resolveGenericParameterTypeArguments(typeArgumentByName), false)
                symbolTable.assign(it.parameter.transformedRefName!!, value)
                callArguments[index] = value
            }

            return clazz.construct(this@Interpreter, callArguments as Array<RuntimeValue>, typeArguments.map { symbolTable().assertToDataType(it) }.toTypedArray(), position)
        } finally {
            callStack.pop(ScopeType.ClassInitializer)
        }
    }

    fun constructClassInstance(callArguments: Array<RuntimeValue>, callPosition: SourcePosition, typeArguments: Array<DataType>, clazz: ClassDefinition): ClassInstance {
        val parentInstance = clazz.superClassInvocation?.let { superClassInvocation ->
            callStack.push("super", ScopeType.Class, SourcePosition("TODO", 1, 1)) // TODO filename
            typeArguments.forEachIndexed { index, dataType ->
                val typeParameter = clazz.typeParameters[index]
                symbolTable().declareTypeAlias(typeParameter.position, typeParameter.name, typeParameter.typeUpperBound)
                symbolTable().declareTypeAliasResolution(typeParameter.position, typeParameter.name, dataType)
            }
            try {
                superClassInvocation.eval() as ClassInstance?
            } finally {
                callStack.pop(ScopeType.Class)
            }
        }

        val symbolTable = callStack.currentSymbolTable()
        val instance = ClassInstance(symbolTable, clazz.fullQualifiedName, clazz, typeArguments.toList(), parentInstance = parentInstance)
        val properties = clazz.primaryConstructor?.parameters?.filter { it.isProperty }?.map { it.parameter.transformedRefName!! }?.toMutableSet() ?: mutableSetOf()

        val nonPropertyArguments = mutableMapOf<String, Pair<ClassParameterNode, RuntimeValue>>()
        clazz.primaryConstructor?.parameters?.forEachIndexed { index, it ->
            val value = callArguments[index]
            if (it.isProperty) {
                instance.assign(name = it.parameter.transformedRefName!!, value = value)
//                    instance.memberPropertyValues[it.parameter.transformedRefName!!] = value
            } else {
                nonPropertyArguments[it.parameter.transformedRefName!!] = Pair(it, value)
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
        symbolTable.declareProperty(callPosition, "this", TypeNode(callPosition, instance.clazz!!.name, typeArguments.map { it.toTypeNode() }.emptyToNull(), false), false)
        symbolTable.assign("this", instance)
        symbolTable.declareProperty(callPosition, "this/${instance.clazz!!.fullQualifiedName}", TypeNode(callPosition, instance.clazz!!.name, typeArguments.map { it.toTypeNode() }.emptyToNull(), false), false)
        symbolTable.assign("this/${instance.clazz!!.fullQualifiedName}", instance)
        symbolTable.registerTransformedSymbol(callPosition, IdentifierClassifier.Property, "this", "this")
        symbolTable.registerTransformedSymbol(callPosition, IdentifierClassifier.Property, "this/${instance.clazz!!.fullQualifiedName}", "this")
//            instance.memberPropertyValues.forEach {
//                symbolTable.putPropertyHolder(instance.clazz!!.memberPropertyNameToTransformedName[it.key]!!, it.value)
//            }

        if (instance.clazz?.superClass != null) {
            // a hack to resolve the "super" keyword. See documentation
            symbolTable.declareProperty(
                callPosition,
                "super",
                TypeNode(SourcePosition.NONE, instance.clazz!!.name, typeArguments.map { it.toTypeNode() }.emptyToNull(), false),
                false
            )
            symbolTable.assign("super", instance)
        }

        val typeParametersAndArguments = clazz.typeParameters.mapIndexed { index, tp ->
            TypeParameterNode(tp.position, tp.name, typeArguments[index].toTypeNode())
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
                    innerSymbolTable.declareProperty(callPosition, it.value.first.transformedRefNameInBody!!, it.value.first.parameter.type.resolveGenericParameterTypeArguments(typeArgumentByName), false)
                    innerSymbolTable.assign(it.value.first.transformedRefNameInBody!!, it.value.second)
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
                            position = it.position,
                            name = "init",
                            declaredReturnType = TypeNode(it.position, "Unit", null, false),
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

    fun FunctionCallNode.evalClassMemberAnyFunctionCall(subject: RuntimeValue, function: FunctionDeclarationNode, replaceArguments: Map<Int, RuntimeValue> = emptyMap()): RuntimeValue {
        callStack.push(functionFullQualifiedName = "class", scopeType = ScopeType.ClassMemberFunction, callPosition = this.position)
        try {
            val symbolTable = callStack.currentSymbolTable()
            if (subject.type() is ObjectType) {
                var clazz: ClassDefinition? = (subject.type() as ObjectType).clazz
                while (clazz != null) {
                    symbolTable.declareProperty(position, "this/${clazz.name}", subject.type().toTypeNode(), false)
                    symbolTable.assign("this/${clazz.name}", subject)
                    clazz = clazz.superClass
                }
            } else {
                symbolTable.declareProperty(position, "this/${subject.type().name}", subject.type().toTypeNode(), false)
                symbolTable.assign("this/${subject.type().name}", subject)
            }
            if (function.receiver != null && function.receiver!!.descriptiveName() != subject.type().name) {
                symbolTable.declareProperty(position, "this/${function.receiver!!.descriptiveName()}", subject.type().toTypeNode(), false)
                symbolTable.assign("this/${function.receiver!!.descriptiveName()}", subject)
            }
            symbolTable.declareProperty(position, "this", subject.type().toTypeNode(), false)
            symbolTable.assign("this", subject)
            symbolTable.registerTransformedSymbol(position, IdentifierClassifier.Property, "this", "this")
            symbolTable.registerTransformedSymbol(position, IdentifierClassifier.Property, "this/${subject.type().name}", "this")

            if (subject is ClassInstance) {
                // a hack to resolve "super". See documentation
                val parentInstance = subject.parentInstance ?: subject
                symbolTable.declareProperty(position, "super", subject.type().toTypeNode(), false)
                symbolTable.assign("super", subject)
                symbolTable.registerTransformedSymbol(position, IdentifierClassifier.Property, "super", "super")
            }

//            // TODO optimize to only copy needed members
//            if (subject is ClassInstance) {
//                subject.memberPropertyValues.forEach {
//                    symbolTable.putPropertyHolder(subject.clazz!!.memberPropertyNameToTransformedName[it.key]!!, it.value)
//                }
//            }

            val instanceGenericTypeResolutions = if (subject is ClassInstance) {
                subject.clazz!!.typeParameters.mapIndexed { index, it ->
                    TypeParameterNode(it.position, it.name, subject.typeArguments[index].toTypeNode())
                }
            } else emptyList()

            val result = evalFunctionCall(
                callNode = this.copy(function = function),
                functionNode = function,
                extraScopeParameters = emptyMap(),
                extraTypeResolutions = instanceGenericTypeResolutions /* type arguments */,
                replaceArguments = replaceArguments,
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
        throw NormalReturnException(returnToAddress = returnToAddress, returnToLabel = returnToLabel, value = value)
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
            position = position,
            name = fullQualifiedName,
            arguments = typeParameters.map { TypeNode(it.position, it.name, null, false) }.emptyToNull(),
            isNullable = false,
        )

        val interfaceInvocations: List<TypeNode>
        val superClassInvocation: FunctionCallNode?
        if (isInterface) {
            superInvocations?.firstOrNull { it is FunctionCallNode }
                ?.let {
                    throw RuntimeException("Interface cannot inherit a class")
                }
            interfaceInvocations = superInvocations?.filterIsInstance<TypeNode>() ?: emptyList()
            superClassInvocation = null
        } else {
            val superClassInvocations = superInvocations?.filterIsInstance<FunctionCallNode>()
            if ((superClassInvocations?.size ?: 0) > 1) {
                throw RuntimeException("A class can only inherit at most one other class")
            }
            interfaceInvocations = superInvocations?.filterIsInstance<TypeNode>() ?: emptyList()
            superClassInvocation = superClassInvocations?.firstOrNull()
        }

        val superClass = (superClassInvocation?.function as? TypeNode)
            ?.let { declarationScope.findClass(it.name) ?: throw RuntimeException("Super class `${it.name}` not found") }
            ?.first

        val clazz: ClassDefinition
        callStack.push(fullQualifiedName, ScopeType.Class, position)
        try {
            typeParameters.forEach {
                callStack.currentSymbolTable().declareTypeAlias(position, it.name, it.typeUpperBound)
            }

            declarationScope.declareClass(position, ClassDefinition(
                currentScope = callStack.currentSymbolTable(),
                name = name,
                modifiers = modifiers,
                isInterface = isInterface,
                fullQualifiedName = fullQualifiedName,
                typeParameters = typeParameters,
                isInstanceCreationAllowed = true,
                primaryConstructor = primaryConstructor,
                rawMemberProperties = ((primaryConstructor?.parameters
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
                        )
                    } ?: emptyList()) +
                        declarations.filterIsInstance<PropertyDeclarationNode>()),
                memberFunctions = declarations
                    .filterIsInstance<FunctionDeclarationNode>()
                    .filter { it.receiver == null }
                    .associateBy { it.transformedRefName!! },
                orderedInitializersAndPropertyDeclarations = declarations
                    .filter { it is ClassInstanceInitializerNode || it is PropertyDeclarationNode },
                declarations = declarations,
                superClassInvocation = superClassInvocation,
                superClass = superClass,
                superInterfaceTypes = interfaceInvocations,
                superInterfaces = interfaceInvocations.map {
                    val clazz = symbolTable().findClass(it.name)?.first
                        ?: throw RuntimeException("Interface ${it.name} cannot be found")
                    if (!clazz.isInterface) {
                        throw RuntimeException("${it.name} is not an interface")
                    }
                    clazz
                },
            ).also { clazz = it })
            // register extension functions in global scope
            declarations
                .filterIsInstance<FunctionDeclarationNode>()
                .filter { it.receiver != null }
                .forEach { globalScope.declareExtensionFunction(it.position, it.transformedRefName!!, it) }
        } finally {
            callStack.pop(ScopeType.Class)
        }

        // companion object
        // TODO create only if a companion object is declared
        callStack.currentSymbolTable().declareClass(position, ClassDefinition(
            currentScope = callStack.currentSymbolTable(),
            name = "$name.Companion",
            fullQualifiedName = "$fullQualifiedName.Companion",
            modifiers = emptySet(),
            typeParameters = emptyList(),
            isInstanceCreationAllowed = false,
            orderedInitializersAndPropertyDeclarations = emptyList(),
            declarations = emptyList(),
            rawMemberProperties = emptyList(),
            memberFunctions = buildList {
                if (ClassModifier.enum in modifiers) {
                    add(CustomFunctionDeclarationNode(
                        CustomFunctionDefinition(
                            position = position,
                            receiverType = "$fullQualifiedName.Companion",
                            functionName = "valueOf",
                            returnType = classType.descriptiveName(),
                            parameterTypes = listOf(CustomFunctionParameter("value", "String")),
                            executable = { interpreter, receiver, args, typeArgs ->
                                val value: String = (args[0] as StringValue).value
                                clazz.enumValues[value]
                                    ?: throwEvalRuntimeException(
                                        position,
                                        "Enum value '$value' not found in class $name"
                                    )
                            }
                        ),
                        transformedRefName = executionEnvironment.findGeneratedMapping(
                            type = ExecutionEnvironment.SymbolType.Function,
                            receiverType = "$fullQualifiedName.Companion",
                            name = "valueOf",
                        ).transformedName,
                    ).also {
                        it.valueParameters.forEach { p ->
                            p.transformedRefName = executionEnvironment.findGeneratedMapping(
                                type = ExecutionEnvironment.SymbolType.ValueParameter,
                                receiverType = it.receiver!!.descriptiveName(),
                                parentName = it.name,
                                name = p.name,
                            ).transformedName
                        }
                    })
                }
            }.associateBy { it.transformedRefName!! },
            primaryConstructor = null
        ))

        // creating enum values
        if (ClassModifier.enum in modifiers) {
            clazz.enumValues = enumEntries.associate {
                val instance = it.call!!.eval() as ClassInstance
                it.name to instance
            }

            ExtensionProperty(
                declaredName = "entries",
                receiver = "$fullQualifiedName.Companion",
                type = "List<${classType.descriptiveName()}>",
                getter = { interpreter, _, _ ->
                    ListValue(clazz.enumValues.values.toList() as List<RuntimeValue>, interpreter.symbolTable().assertToDataType(classType), interpreter.symbolTable())
                }
            ).also {
                val transformedName = executionEnvironment.findGeneratedMapping(
                    type = ExecutionEnvironment.SymbolType.ExtensionProperty,
                    receiverType = "$fullQualifiedName.Companion",
                    name = "entries",
                ).transformedName
                it.transformedName = transformedName
                symbolTable().declareExtensionProperty(position, transformedName, it)
            }
        }
    }

    fun NavigationNode.eval(): RuntimeValue {
        val obj = (subject.eval() as RuntimeValue)
            .let { resolveSuperKeyword(it) }
//        return obj.memberPropertyValues[member.transformedRefName!!]!!

        if (memberType == NavigationNode.MemberType.Extension && transformedRefName != null) {
            val extensionProperty = symbolTable().findExtensionProperty(transformedRefName!!)
                ?: throw RuntimeException("Extension property `${member.name}` on receiver `${obj.type().nameWithNullable}` could not be found")

            if (obj == NullValue && !extensionProperty.receiverType!!.isNullable) {
                if (operator == ".") {
                    throw EvaluateNullPointerException(callStack.currentSymbolTable(), callStack.getStacktrace(position))
                } else if (operator == "?.") {
                    return obj
                }
            }

            val typeArgumentsMap = extensionProperty.typeArgumentsMap(obj.type())

            extensionProperty.getter?.let { getter ->
                return getter(this@Interpreter, obj, typeArgumentsMap)
            }
        }
        if (memberType == NavigationNode.MemberType.Enum) {
            val originalClassName = (obj as ClassInstance).clazz!!.fullQualifiedName.removeSuffix(".Companion")
            val enumClazz = symbolTable().findClass(originalClassName)?.first
                ?: throw RuntimeException("Cannot find class $originalClassName")
            if (ClassModifier.enum !in enumClazz.modifiers) {
                throw RuntimeException("Class `$originalClassName` is not an enum class")
            }
            return enumClazz.enumValues[member.name]
                ?: throw RuntimeException("No such enum `${member.name}` in class `$originalClassName`")
        }

        if (obj == NullValue) throw EvaluateNullPointerException(callStack.currentSymbolTable(), callStack.getStacktrace(position))
        obj as? ClassInstance ?: throw RuntimeException("Cannot access member `${member.name}` for type `${obj.type().nameWithNullable}`")
        // before type resolution is implemented in SemanticAnalyzer, reflect from clazz as a slower alternative
        return when (val r = obj.read(interpreter = this@Interpreter, name = obj.clazz!!.findMemberPropertyTransformedName(member.name)!!)) {
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

    fun IndexOpNode.eval(): RuntimeValue {
        if (hasFunctionCall == true) {
            return call!!.eval()
        } else {
            return subject.eval() as RuntimeValue
        }
    }

    fun AsOpNode.eval(): RuntimeValue {
        val value = expression.eval() as RuntimeValue
        val targetType = symbolTable().typeNodeToDataType(type) ?: throw RuntimeException("Unknown type `${type.descriptiveName()}`")
        return if (targetType.isConvertibleFrom(value.type())) {
            value
        } else if (isNullable) {
            NullValue
        } else {
            throw EvaluateTypeCastException(symbolTable(), callStack.getStacktrace(position), value.type().descriptiveName, targetType.descriptiveName)
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
            runtimeRefs.declareFunction(position, it, currentSymbolTable.findFunction(it)!!.first)
        }
        refs.classes.forEach {
            runtimeRefs.declareClass(position, currentSymbolTable.findClass(it)!!.first)
        }
        refs.typeAlias.forEach {
//            runtimeRefs.declareTypeAlias(it, currentSymbolTable.findTypeAlias(it)!!.first.toTypeNode(), currentSymbolTable)
            val resolution = currentSymbolTable.findTypeAliasResolution(it)!!.toTypeNode()
            runtimeRefs.declareTypeAlias(position, it, currentSymbolTable.findTypeAlias(it)!!.first.toTypeNode(), currentSymbolTable)
            runtimeRefs.declareTypeAliasResolution(position, it, resolution, currentSymbolTable)
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

    fun InfixFunctionCallNode.eval(): RuntimeValue {
        val n1 = node1.eval() as RuntimeValue
        return when (functionName) {
            "to" -> {
                val n2 = node2.eval() as RuntimeValue
                PairValue(n1 to n2, n1.type(), n2.type(), symbolTable())
            }
            "is", "!is" -> {
                val type = symbolTable().assertToDataType(node2 as TypeNode)
                val isType = type.isAssignableFrom(n1.type())
                BooleanValue(if (functionName == "is") isType else !isType)
            }
            else -> throw RuntimeException("Unknown infix function `$functionName`")
        }
    }

    fun ElvisOpNode.eval(): RuntimeValue {
        val result = primaryNode.eval() as RuntimeValue
        if (result != NullValue) {
            return result
        }
        return fallbackNode.eval() as RuntimeValue
    }

    fun ThrowNode.eval(): RuntimeValue {
        var initialResult = value.eval() as RuntimeValue
        var result: RuntimeValue? = initialResult
        while (result !is ThrowableValue && result is ClassInstance) {
            result = result.parentInstance
        }
        if (result !is ThrowableValue) {
            throw EvaluateTypeCastException(
                currentScope = symbolTable(),
                stacktrace = callStack.getStacktrace(position),
                valueType = initialResult.type().descriptiveName,
                targetType = "Throwable",
            )
        }
        result = ThrowableValue(
            currentScope = symbolTable(),
            message = result.message,
            cause = result.cause,
            stacktrace = result.stacktrace,
            thisClazz = symbolTable().findClass(initialResult.type().name)!!.first,
        )
        throw EvaluateRuntimeException(stacktrace = callStack.getStacktrace(position), error = result)
    }

    fun throwEvalRuntimeException(position: SourcePosition, message: String): Nothing {
        val stacktrace = callStack.getStacktrace(position)
        val error = ExceptionValue(
            currentScope = symbolTable(),
            message = message,
            cause = null,
            stacktrace = stacktrace,
            thisClazz = ExceptionValue.clazz,
        )
        throw EvaluateRuntimeException(stacktrace = stacktrace, error = error)
    }

    fun TryNode.eval(): RuntimeValue {
        try {
            return mainBlock.eval() as RuntimeValue
        } catch (e: EvaluateRuntimeException) {
            for (catch in catchBlocks) {
                if (symbolTable().assertToDataType(catch.catchType).isAssignableFrom(e.error.type())) {
                    return catch.eval(e.error)
                }
            }
            throw e
        } catch (e: Throwable) {
            for (catch in catchBlocks) {
                if (catch.catchType.name == "Throwable") {
                    return catch.eval(e.toValue())
                }
            }
            throw e
        } finally {
            finallyBlock?.eval()
        }
    }

    fun Throwable.toValue(): ThrowableValue {
        return ThrowableValue(symbolTable(), message, cause?.toValue(), emptyList(), this.fullClassName)
    }

    fun CatchNode.eval(value: ThrowableValue): RuntimeValue {
        callStack.push("<catch>", ScopeType.Catch, position)
        return try {
            valueTransformedRefName?.let { valueTransformedRefName ->
                symbolTable().declareProperty(
                    position = position,
                    name = valueTransformedRefName,
                    type = catchType,
                    isMutable = false,
                )
                symbolTable().assign(name = valueTransformedRefName, value = value)
            }
            block.eval()
        } finally {
            callStack.pop(ScopeType.Catch)
        }
    }

    fun WhenNode.eval(): RuntimeValue {
        callStack.push("<when>", ScopeType.WhenOuter, position)
        try {
            val subjectValue = subject?.value?.eval() as? RuntimeValue ?: UnitValue
            if (subject?.hasValueDeclaration() == true) {
                subject.valueTransformedRefName?.let { valueTransformedRefName ->
                    symbolTable().declareProperty(
                        position = position,
                        name = valueTransformedRefName,
                        type = subject.type!!,
                        isMutable = false,
                    )
                    symbolTable().assign(name = valueTransformedRefName, value = subjectValue)
                }
            }
            entries.forEach { entry ->
                if (entry.conditions.isEmpty() || entry.conditions.any {
                        if (it.testType == WhenConditionNode.TestType.TypeTest) {
                            val type = symbolTable().assertToDataType(it.expression as TypeNode)
                            return@any type.isAssignableFrom(subjectValue.type())
                        }
                        val evalExprResult = it.expression.eval()
                        if (subject == null) {
                            return@any (evalExprResult as BooleanValue).value
                        } else {
                            return@any evalExprResult == subjectValue
                        }
                    }
                ) {
                    return entry.body.eval()
                }
            }
            throw RuntimeException("No match for `when` expression at $position")
        } finally {
            callStack.pop(ScopeType.WhenOuter)
        }
    }

    fun ForNode.eval(): RuntimeValue {
        val subjectValue = subject.eval() as RuntimeValue
        callStack.push("<for>", ScopeType.For, position)

        fun FunctionCallNode.enrichIterableCall(receiverType: DataType): FunctionCallNode {
            val functionName = (function as NavigationNode).member.name
            val actualFunction = symbolTable().findFunctionOrExtensionFunctionIncludingSuperclassesByDeclaredName(
                receiverType.toTypeNode(), functionName
            ).single()
            val functionReceiverType = actualFunction.receiver!!
            val functionTypeParameters = actualFunction.typeParameters
            val inferredTypeArguments: List<TypeNode>

            if (functionTypeParameters.isNotEmpty()) {
                var type: DataType? = receiverType
                if (type != null && type.name != functionReceiverType.name) {
                    type = (type as? ObjectType)?.findSuperType(functionReceiverType.name)
                }
                if (type == null && type !is ObjectType) {
                    throw RuntimeException("Enrich fail -- Receiver type of `$functionName` ${functionReceiverType.descriptiveName()} is not found")
                }
                val functionReceiverClazzTypeParameters = (type as ObjectType).clazz.typeParameters
                val functionReceiverClazzTypeArguments = (type as ObjectType).arguments
                val functionReceiverClazzTypeArgumentsMap = functionReceiverClazzTypeParameters.mapIndexed { i, tp ->
                    tp.name to functionReceiverClazzTypeArguments[i]
                }.toMap()
                inferredTypeArguments = functionTypeParameters.map {
                    functionReceiverClazzTypeArgumentsMap[it.name]!!.toTypeNode()
                }
            } else {
                inferredTypeArguments = emptyList()
            }

            return copy(
                functionRefName = symbolTable().findFunctionOrExtensionFunctionIncludingSuperclassesByDeclaredName(
                    receiverType.toTypeNode(), functionName
                ).single().transformedRefName,
                inferredTypeArguments = inferredTypeArguments,
            )
        }

        try {
            symbolTable().declareProperty(subject.position, "#subject", subjectValue.type().toTypeNode(), false)
            symbolTable().assign("#subject", subjectValue)

            // TODO move the call lookups to Semantic Analyzer. Currently impossible because runtime class type member always has higher priority than compile-time type
            val iteratorValue = FunctionCallNode(
                function = NavigationNode(
                    position = position,
                    subject = VariableReferenceNode(position, "#subject"),
                    operator = ".",
                    member = ClassMemberReferenceNode(position, "iterator")
                ),
                arguments = emptyList(),
                declaredTypeArguments = emptyList(),
                position = position,
                callableType = CallableType.ExtensionFunction,
            ).enrichIterableCall(subjectValue.type()).eval()
            symbolTable().declareProperty(subject.position, "#iterator", iteratorValue.type().toTypeNode(), false)
            symbolTable().assign("#iterator", iteratorValue)
            val hasNextCall = FunctionCallNode(
                function = NavigationNode(
                    position = position,
                    subject = VariableReferenceNode(position, "#iterator"),
                    operator = ".",
                    member = ClassMemberReferenceNode(position, "hasNext")
                ),
                arguments = emptyList(),
                declaredTypeArguments = emptyList(),
                position = position,
                callableType = CallableType.ExtensionFunction,
            ).enrichIterableCall(iteratorValue.type())
            val nextCall = FunctionCallNode(
                function = NavigationNode(
                    position = position,
                    subject = VariableReferenceNode(position, "#iterator"),
                    operator = ".",
                    member = ClassMemberReferenceNode(position, "next")
                ),
                arguments = emptyList(),
                declaredTypeArguments = emptyList(),
                position = position,
                callableType = CallableType.ExtensionFunction,
            ).enrichIterableCall(iteratorValue.type())

            while ((hasNextCall.eval() as BooleanValue).value) {
                val nextValue = nextCall.eval()

                variables.forEach {
                    symbolTable().declareProperty(
                        position = position,
                        name = it.transformedRefName!!,
                        type = it.type,
                        isMutable = false,
                    )
                    symbolTable().assign(name = it.transformedRefName!!, value = nextValue)
                }

                body.eval()

                variables.forEach {
                    symbolTable().undeclareProperty(it.transformedRefName!!)
                }
            }
        } finally {
            callStack.pop(ScopeType.For)
        }
        return UnitValue
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
        is IndexOpNode -> this.declaredType()
        else -> throw UnsupportedOperationException()
    }


    fun NavigationNode.declaredType(): DataType {
        return callStack.currentSymbolTable().typeNodeToPropertyType(type!!, false)!!.type
    }

    fun VariableReferenceNode.declaredType(): DataType {
        return callStack.currentSymbolTable().typeNodeToPropertyType(type!!, false)!!.type
    }

    fun IndexOpNode.declaredType(): DataType {
        return callStack.currentSymbolTable().assertToDataType(call!!.returnType!!)
    }

    fun eval() = scriptNode.eval()

}
