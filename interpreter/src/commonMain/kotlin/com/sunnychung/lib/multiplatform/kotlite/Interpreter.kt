package com.sunnychung.lib.multiplatform.kotlite

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
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode

class Interpreter(val scriptNode: ScriptNode) {

    val callStack = CallStack()

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
                    val variable = (node as VariableReferenceNode).transformedRefName!!
                    val newValue = result + IntValue(1)
                    callStack.currentSymbolTable().assign(variable, newValue)
                    newValue
                }
                "pre--" -> {
                    val variable = (node as VariableReferenceNode).transformedRefName!!
                    val newValue = result - IntValue(1)
                    callStack.currentSymbolTable().assign(variable, newValue)
                    newValue
                }
                "post++" -> {
                    val variable = (node as VariableReferenceNode).transformedRefName!!
                    val newValue = result + IntValue(1)
                    callStack.currentSymbolTable().assign(variable, newValue)
                    result
                }
                "post--" -> {
                    val variable = (node as VariableReferenceNode).transformedRefName!!
                    val newValue = result - IntValue(1)
                    callStack.currentSymbolTable().assign(variable, newValue)
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
            symbolTable.declareProperty(name, type)
            symbolTable.assign(name, value as RuntimeValue)
        } else {
            symbolTable.declareProperty(name, type)
        }
    }

    fun AssignmentNode.eval() {
        if (subject is NavigationNode && subject.operator == "?.") {
            throw UnsupportedOperationException("?: on left side of assignment is not supported")
        }
        val result = value.eval()

        val read = { subject.eval() }
        val write = { value: RuntimeValue ->
            when (subject) {
                is VariableReferenceNode -> {
                    callStack.currentSymbolTable().assign(subject.transformedRefName ?: subject.variableName, value)
                }

                is NavigationNode -> {
                    val obj = subject.subject.eval() as ClassInstance
//                    obj.assign((subject.member as ClassMemberReferenceNode).transformedRefName!!, value)
                    // before type resolution is implemented in SemanticAnalyzer, reflect from clazz as a slower alternative
                    obj.assign(obj.clazz.memberProperties[(subject.member as ClassMemberReferenceNode).name]!!.transformedRefName!!, value)
                }

                else -> throw UnsupportedOperationException()
            }
        }

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
        return callStack.currentSymbolTable().read(transformedRefName ?: variableName)
    }

    fun FunctionDeclarationNode.eval() {
        callStack.currentSymbolTable().declareFunction(name, this)
    }

    fun FunctionCallNode.eval(): RuntimeValue {
        // TODO move to semantic analyzer
        val name = (function as? VariableReferenceNode ?: throw UnsupportedOperationException("Dynamic functions are not yet supported")).variableName
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

        callStack.push(functionFullQualifiedName = functionNode.name, scopeType = ScopeType.Function, callPosition = callNode.position)
        try {
            val symbolTable = callStack.currentSymbolTable()
            extraScopeParameters.forEach {
                symbolTable.declareProperty(it.key, TypeNode(it.value.type().toString(), null, false)) // TODO change to use DataType directly
                symbolTable.assign(it.key, it.value)
            }
            functionNode.valueParameters.forEachIndexed { index, it ->
                symbolTable.declareProperty(it.transformedRefName!!, it.type)
                symbolTable.assign(it.transformedRefName!!, callArguments[index] ?: (it.defaultValue!!.eval() as RuntimeValue))
            }

            // execute function
            val returnValue = try {
                functionNode.body.eval()
            } catch (r: NormalReturnException) {
                r.value
            }

            log.v { "Fun Return $returnValue; symbolTable = $symbolTable" }
            return FunctionCallResult(returnValue, symbolTable)
        } finally {
            callStack.pop(ScopeType.Function)
        }
    }

    fun FunctionCallNode.evalCreateClassInstance(clazz: ClassDefinition): ClassInstance {
        // TODO call constructor and initializer

        val instance = ClassInstance(clazz)
//        clazz.memberProperties.forEach { (name, declaration) ->
//            declaration.initialValue?.eval()
//                ?.also { instance.memberPropertyValues[name] = it as RuntimeValue }
//        }
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
            val nonPropertyArguments = mutableMapOf<String, RuntimeValue>()
            clazz.primaryConstructor?.parameters?.forEachIndexed { index, it ->
                // no need to use transformedRefName as duplicated declarations are not possible here
                val value = callArguments[index] ?: (it.parameter.defaultValue!!.eval() as RuntimeValue)
                symbolTable.declareProperty(it.parameter.transformedRefName!!, it.parameter.type)
                symbolTable.assign(it.parameter.transformedRefName!!, value)
                if (it.isProperty) {
                    instance.memberPropertyValues[it.parameter.transformedRefName!!] = value
                } else {
                    nonPropertyArguments[it.parameter.transformedRefName!!] = value
                }
            }

            // move nonPropertyArguments from outer scope into inner scope
            nonPropertyArguments.keys.forEach {
                symbolTable.undeclareProperty(it)
//                symbolTable.undeclarePropertyByDeclaredName(it)
            }

            // variable "this" is available after primary constructor
            symbolTable.declareProperty("this", TypeNode("", null, false))
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
                        innerSymbolTable.declareProperty(it.key, TypeNode("", null, false))
                        innerSymbolTable.assign(it.key, it.value)
                    }
                    when (it) {
                        is PropertyDeclarationNode -> {
                            properties += it.transformedRefName!!
                            val value = it.initialValue!!.eval() as RuntimeValue
                            // constructor parameter has higher priority than instance member variables
//                        if (!symbolTable.hasProperty(it.name, isThisScopeOnly = true)) {
                            symbolTable.declareProperty(it.transformedRefName!!, it.type)
                            symbolTable.assign(it.transformedRefName!!, value)
//                        }
                            instance.memberPropertyValues[it.transformedRefName!!] = value
                        }

                        is ClassInstanceInitializerNode -> {
                            val init = FunctionDeclarationNode(
                                "init",
                                TypeNode("Unit", null, false),
                                emptyList(),
                                it.block
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

            properties.forEach {
                // TODO check for isModifiable
                // TODO type checking
                val possiblyModifiedValue = symbolTable.read(it, isThisScopeOnly = true)
                instance.memberPropertyValues[it] = possiblyModifiedValue
            }
        } finally {
            callStack.pop(ScopeType.ClassInitializer)
        }

        return instance
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
            name = name,
            fullQualifiedName = fullQualifiedName,
            isInstanceCreationAllowed = true,
            primaryConstructor = primaryConstructor,
            memberProperties = ((primaryConstructor?.parameters
                ?.filter { it.isProperty }
                ?.map { it.parameter }
                ?.map { PropertyDeclarationNode(
                    name = it.name,
                    type = it.type,
                    initialValue = it.defaultValue,
                    transformedRefName = it.transformedRefName
                ) } ?: emptyList()) +
                    declarations.filterIsInstance<PropertyDeclarationNode>())
                .associateBy { it.name }
            ,
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
        return obj.read(obj.clazz.memberProperties[member.name]!!.transformedRefName!!)
    }

    fun IntegerNode.eval() = IntValue(value)
    fun DoubleNode.eval() = DoubleValue(value)
    fun BooleanNode.eval() = BooleanValue(value)
    fun NullNode.eval() = NullValue

    fun eval() = scriptNode.eval()

}
