package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateNullPointerException
import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateRuntimeException
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalBreakException
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalContinueException
import com.sunnychung.lib.multiplatform.kotlite.error.controlflow.NormalReturnException
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.CallStack
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallResult
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
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
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue
import com.sunnychung.lib.multiplatform.kotlite.model.ValueNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode

class Interpreter(val scriptNode: ScriptNode) {

    internal val callStack = CallStack()

    fun DataType.toTypeNode() = TypeNode(name, null, isNullable)

    fun ASTNode.eval(): Any {
        return when (this) {
            is AssignmentNode -> this.eval()
            is BinaryOpNode -> this.eval()
            is IntegerNode -> this.eval()
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
            "+" -> castType<NumberValue<*>, NumberValue<*>>(node1.eval(), node2.eval()) { a, b -> a + b }
            "-" -> castType<NumberValue<*>, NumberValue<*>>(node1.eval(), node2.eval()) { a, b -> a - b }
            "*" -> castType<NumberValue<*>, NumberValue<*>>(node1.eval(), node2.eval()) { a, b -> a * b }
            "/" -> castType<NumberValue<*>, NumberValue<*>>(node1.eval(), node2.eval()) { a, b -> a / b }
            "%" -> castType<NumberValue<*>, NumberValue<*>>(node1.eval(), node2.eval()) { a, b -> a % b }

            "<" -> castType<NumberValue<*>, BooleanValue>(node1.eval(), node2.eval()) { a, b -> BooleanValue(a < b) }
            "<=" -> castType<NumberValue<*>, BooleanValue>(node1.eval(), node2.eval()) { a, b -> BooleanValue(a <= b) }
            ">" -> castType<NumberValue<*>, BooleanValue>(node1.eval(), node2.eval()) { a, b -> BooleanValue(a > b) }
            ">=" -> castType<NumberValue<*>, BooleanValue>(node1.eval(), node2.eval()) { a, b -> BooleanValue(a >= b) }
            "==" -> castType<NumberValue<*>, BooleanValue>(node1.eval(), node2.eval()) { a, b -> BooleanValue(a == b) }
            "!=" -> castType<NumberValue<*>, BooleanValue>(node1.eval(), node2.eval()) { a, b -> BooleanValue(a != b) }

            "||" -> castType<BooleanValue, BooleanValue>(node1.eval()) { a -> if (a.value) BooleanValue(true) else node2.eval() as BooleanValue }
            "&&" -> castType<BooleanValue, BooleanValue>(node1.eval()) { a -> if (!a.value) BooleanValue(false) else node2.eval() as BooleanValue }

            else -> throw UnsupportedOperationException()
        }
    }

    fun UnaryOpNode.eval(): RuntimeValue {
        val result = node!!.eval()
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
                    NavigationNode(VariableReferenceNode("this"), ".", ClassMemberReferenceNode(this.variableName, this.transformedRefName)).write(value)
                } else {
                    callStack.currentSymbolTable().assign(this.transformedRefName ?: this.variableName, value)
                }
            }

            is NavigationNode -> {
                val obj = this.subject.eval() as ClassInstance
//                    obj.assign((subject.member as ClassMemberReferenceNode).transformedRefName!!, value)
                // before type resolution is implemented in SemanticAnalyzer, reflect from clazz as a slower alternative
                obj.assign(obj.clazz.memberPropertyNameToTransformedName[(this.member as ClassMemberReferenceNode).name]!!, value)?.also {
                    FunctionCallNode(
                        it,
                        listOf(FunctionCallArgumentNode(index = 0, value = ValueNode(value))),
                        SourcePosition(1, 1)
                    ).evalClassMemberAnyFunctionCall(obj, it)
                }
            }

            else -> throw UnsupportedOperationException()
        }
    }

    fun AssignmentNode.eval() {
        if (subject is NavigationNode && subject.operator == "?.") {
            throw UnsupportedOperationException("?: on left side of assignment is not supported")
        }
        val result = value.eval()

        val read = { subject.eval() }
        val write = { value: RuntimeValue -> subject.write(value) }

        val finalResult = if (operator == "=") {
            result as RuntimeValue
        } else {
            val existing = read()
            val newResult = when (operator) {
                "+=" -> (existing as NumberValue<*>) + result as NumberValue<*>
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
            return NavigationNode(VariableReferenceNode("this"), ".", ClassMemberReferenceNode(variableName, transformedRefName)).eval()
        }
        return callStack.currentSymbolTable().read(transformedRefName ?: variableName)
    }

    fun FunctionDeclarationNode.eval() {
        if (receiver == null) {
            callStack.currentSymbolTable().declareFunction(name, this)
        } else {
            callStack.currentSymbolTable().declareExtensionFunction(transformedRefName!!, this)
        }
    }

    fun FunctionCallNode.eval(): RuntimeValue {
        // TODO move to semantic analyzer
        when (function) {
            is VariableReferenceNode -> {
                val name = function.variableName

                if (this.function.ownerRef != null) {
                    return this.copy(function = NavigationNode(VariableReferenceNode(this.function.ownerRef!!), ".", ClassMemberReferenceNode(name))).eval()
                }

                val functionNode = callStack.currentSymbolTable().findFunction(name)
                if (functionNode != null) {
                    return evalFunctionCall(functionNode)
                }
                val classDefinition = callStack.currentSymbolTable().findClass(name)

                if (classDefinition != null) {
                    return evalCreateClassInstance(classDefinition)
                }
                throw RuntimeException("Function $name not found")
            }

            is NavigationNode -> {
                val subject = function.subject.eval()
                if (subject == NullValue) {
                    if (function.operator == "?.") {
                        return NullValue // TODO not always true for extension functions
                    } else {
                        throw EvaluateNullPointerException()
                    }
                }
                if (functionRefName != null) { // an extension function
                    val function = callStack.currentSymbolTable().findExtensionFunction(functionRefName!!)
                        ?: throw RuntimeException("Analysed function $functionRefName not found")
                    return evalClassMemberAnyFunctionCall(subject as RuntimeValue, function)
                }
                return evalClassMemberFunctionCall(subject as ClassInstance, function.member)
            }

            else -> throw UnsupportedOperationException("Dynamic functions are not yet supported")
        }
    }

    fun FunctionCallNode.evalFunctionCall(functionNode: FunctionDeclarationNode): RuntimeValue {
        return evalFunctionCall(this, functionNode, emptyMap()).result
    }

    fun evalFunctionCall(callNode: FunctionCallNode, functionNode: FunctionDeclarationNode, extraScopeParameters: Map<String, RuntimeValue>): FunctionCallResult {
        // TODO optimize to remove most loops
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
        callArguments.forEachIndexed { index, it ->
            val parameterNode = functionNode.valueParameters[index]
            if (it == null && parameterNode.defaultValue == null) {
                throw RuntimeException("Missing parameter `${parameterNode.name} in function call ${functionNode.name}`")
            }
        }

        val returnType = callStack.currentSymbolTable().typeNodeToPropertyType(functionNode.type, false)!!.type

        callStack.push(functionFullQualifiedName = functionNode.name, scopeType = ScopeType.Function, callPosition = callNode.position)
        try {
            val symbolTable = callStack.currentSymbolTable()
            extraScopeParameters.forEach {
                symbolTable.declareProperty(it.key, TypeNode(it.value.type().name, null, false), false) // TODO change to use DataType directly
                symbolTable.assign(it.key, it.value)
            }
            functionNode.valueParameters.forEachIndexed { index, it ->
                symbolTable.declareProperty(it.transformedRefName!!, it.type, false)
                symbolTable.assign(it.transformedRefName!!, callArguments[index] ?: (it.defaultValue!!.eval() as RuntimeValue))
            }

            // execute function
            val returnValue = try {
                val result = functionNode.body.eval()
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
                throw RuntimeException("Type ${returnValue.type().name} cannot be casted to ${returnType.name}")
            }

            return FunctionCallResult(returnValue, symbolTable)
        } finally {
            callStack.pop(ScopeType.Function)
        }
    }

    fun FunctionCallNode.evalCreateClassInstance(clazz: ClassDefinition): ClassInstance {
        val instance = ClassInstance(clazz)
        val properties = clazz.primaryConstructor?.parameters?.filter { it.isProperty }?.map { it.parameter.transformedRefName!! }?.toMutableSet() ?: mutableSetOf()

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

            val symbolTable = callStack.currentSymbolTable()
            val nonPropertyArguments = mutableMapOf<String, Pair<TypeNode, RuntimeValue>>()
            clazz.primaryConstructor?.parameters?.forEachIndexed { index, it ->
                // no need to use transformedRefName as duplicated declarations are not possible here
                val value = callArguments[index] ?: (it.parameter.defaultValue!!.eval() as RuntimeValue)
                symbolTable.declareProperty(it.parameter.transformedRefName!!, it.parameter.type, false)
                symbolTable.assign(it.parameter.transformedRefName!!, value)
                if (it.isProperty) {
                    instance.assign(it.parameter.transformedRefName!!, value)
//                    instance.memberPropertyValues[it.parameter.transformedRefName!!] = value
                } else {
                    nonPropertyArguments[it.parameter.transformedRefName!!] = Pair(it.parameter.type, value)
                }
            }

            // move nonPropertyArguments from outer scope into inner scope
            nonPropertyArguments.keys.forEach {
                symbolTable.undeclareProperty(it)
//                symbolTable.undeclarePropertyByDeclaredName(it)
            }

            // variable "this" is available after primary constructor
            symbolTable.declareProperty("this", TypeNode(instance.clazz.name, null, false), false)
            symbolTable.assign("this", instance)

            clazz.orderedInitializersAndPropertyDeclarations.forEach {
                callStack.push(
                    functionFullQualifiedName = "init-property",
                    scopeType = ScopeType.ClassInitializer,
                    callPosition = this.position
                )
                try {
                    val innerSymbolTable = callStack.currentSymbolTable()
                    nonPropertyArguments.forEach {
                        val keyWithIncreasedScope = it.key.replaceAfterLast("/", innerSymbolTable.scopeLevel.toString())
                        log.v("keyWithIncreasedScope = $keyWithIncreasedScope")
                        innerSymbolTable.declareProperty(keyWithIncreasedScope, it.value.first, false)
                        innerSymbolTable.assign(keyWithIncreasedScope, it.value.second)
                    }
                    when (it) {
                        is PropertyDeclarationNode -> {
                            properties += it.transformedRefName!!
                            val value = it.initialValue?.eval() as RuntimeValue?
                            value?.let { value ->
                                instance.assign(it.transformedRefName!!, value)
                            }
                        }

                        is ClassInstanceInitializerNode -> {
                            val init = FunctionDeclarationNode(
                                name = "init",
                                type = TypeNode("Unit", null, false),
                                valueParameters = emptyList(),
                                body = it.block
                            )
                            evalFunctionCall(
                                callNode = FunctionCallNode(
                                    function = this, /* not used */
                                    arguments = emptyList(),
                                    position = this.position,
                                ),
                                functionNode = init,
                                extraScopeParameters = emptyMap(),
                            )
                        }

                        else -> Unit
                    }
                } finally {
                    callStack.pop(ScopeType.ClassInitializer)
                }
            }
        } finally {
            callStack.pop(ScopeType.ClassInitializer)
        }

        return instance
    }

    fun FunctionCallNode.evalClassMemberFunctionCall(subject: ClassInstance, member: ClassMemberReferenceNode): RuntimeValue {
        val function = subject.clazz.memberFunctions[member.name] ?: throw EvaluateRuntimeException("Member function `${member.name}` not found")
        return evalClassMemberAnyFunctionCall(subject, function)
    }

    fun FunctionCallNode.evalClassMemberAnyFunctionCall(subject: RuntimeValue, function: FunctionDeclarationNode): RuntimeValue {
        callStack.push(functionFullQualifiedName = "class", scopeType = ScopeType.ClassMemberFunction, callPosition = this.position)
        try {
            val symbolTable = callStack.currentSymbolTable()
            symbolTable.declareProperty("this", subject.type().toTypeNode(), false)
            symbolTable.assign("this", subject)

            val result = evalFunctionCall(
                callNode = this.copy(function = function),
                functionNode = function,
                extraScopeParameters = emptyMap(),
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
        callStack.currentSymbolTable().declareClass(ClassDefinition(
            currentScope = callStack.currentSymbolTable(),
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
        ))
    }

    fun NavigationNode.eval(): RuntimeValue {
        val obj = subject.eval() as ClassInstance
//        return obj.memberPropertyValues[member.transformedRefName!!]!!
        // before type resolution is implemented in SemanticAnalyzer, reflect from clazz as a slower alternative
        return when (val r = obj.read(obj.clazz.memberPropertyNameToTransformedName[member.name]!!)) {
            is RuntimeValue -> r
            is FunctionDeclarationNode -> {
                FunctionCallNode(
                    function = r,
                    arguments = emptyList(),
                    position = SourcePosition(1, 1)
                ).evalClassMemberAnyFunctionCall(obj, r)
            }
            else -> throw UnsupportedOperationException()
        }
    }

    fun IntegerNode.eval() = IntValue(value)
    fun DoubleNode.eval() = DoubleValue(value)
    fun BooleanNode.eval() = BooleanValue(value)
    fun NullNode.eval() = NullValue
    fun ValueNode.eval() = value

    fun eval() = scriptNode.eval()

}
