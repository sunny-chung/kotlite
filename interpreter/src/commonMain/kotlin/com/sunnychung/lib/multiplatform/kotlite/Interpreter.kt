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
import com.sunnychung.lib.multiplatform.kotlite.model.CallType
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
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
            is BooleanNode -> this.eval()
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
        }
    }

    fun ScriptNode.eval() {
        nodes.forEach { it.eval() }
    }

    fun BinaryOpNode.eval(): RuntimeValue {
        val result1 = node1.eval() as IntValue
        val result2 = node2.eval() as IntValue
        return when (operator) { // TODO overflow
            "+" -> result1 + result2
            "-" -> result1 - result2
            "*" -> result1 * result2
            "/" -> result1 / result2
            "%" -> result1 % result2

            "<" -> BooleanValue(result1 < result2)
            "<=" -> BooleanValue(result1 <= result2)
            ">" -> BooleanValue(result1 > result2)
            ">=" -> BooleanValue(result1 >= result2)
            "==" -> BooleanValue(result1 == result2)
            "!=" -> BooleanValue(result1 != result2)

            else -> throw UnsupportedOperationException()
        }
    }

    fun UnaryOpNode.eval(): IntValue {
        val result = node!!.eval() as IntValue
        return when (operator) {
            "+" -> IntValue(+ result.value!!)
            "-" -> IntValue(- result.value!!)
            "pre++" -> {
                val variable = (node as VariableReferenceNode).transformedRefName!!
                val newValue = IntValue(result.value + 1)
                callStack.currentSymbolTable().assign(variable, newValue)
                newValue
            }
            "pre--" -> {
                val variable = (node as VariableReferenceNode).transformedRefName!!
                val newValue = IntValue(result.value - 1)
                callStack.currentSymbolTable().assign(variable, newValue)
                newValue
            }
            "post++" -> {
                val variable = (node as VariableReferenceNode).transformedRefName!!
                val newValue = IntValue(result.value + 1)
                callStack.currentSymbolTable().assign(variable, newValue)
                result
            }
            "post--" -> {
                val variable = (node as VariableReferenceNode).transformedRefName!!
                val newValue = IntValue(result.value - 1)
                callStack.currentSymbolTable().assign(variable, newValue)
                result
            }
            else -> throw UnsupportedOperationException()
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
        val result = value.eval()
        val finalResult = if (operator == "=") {
            result as RuntimeValue
        } else {
            val existing = callStack.currentSymbolTable().read(transformedRefName!!)
            val newResult = when (operator) {
                "+=" -> (existing as IntValue) + result as IntValue
                "-=" -> (existing as IntValue) - result as IntValue
                "*=" -> (existing as IntValue) * result as IntValue
                "/=" -> (existing as IntValue) / result as IntValue
                "%=" -> (existing as IntValue) % result as IntValue
                else -> throw UnsupportedOperationException()
            }
            newResult
        }
        callStack.currentSymbolTable().assign(transformedRefName!!, finalResult)
    }

    fun VariableReferenceNode.eval(): IntValue {
        return callStack.currentSymbolTable().read(transformedRefName!!) as IntValue
    }

    fun FunctionDeclarationNode.eval() {
        callStack.currentSymbolTable().declareFunction(name, this)
    }

    fun FunctionCallNode.eval(): RuntimeValue {
        // TODO move to semantic analyzer
        // TODO optimize to remove most loops
        val functionName = (function as? VariableReferenceNode ?: throw UnsupportedOperationException("Dynamic functions are not yet supported")).variableName
        val functionNode = callStack.currentSymbolTable().findFunction(functionName) ?: throw RuntimeException("Function $functionName not found")
        val callArguments = arrayOfNulls<RuntimeValue>(functionNode.valueParameters.size)
        arguments.forEach { a ->
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
                throw RuntimeException("Missing parameter `${parameterNode.name} in function call $functionName`")
            }
        }

        callStack.push(functionFullQualifiedName = functionName, callType = CallType.Function, callPosition = position)
        try {
            val symbolTable = callStack.currentSymbolTable()
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
            return returnValue
        } finally {
            callStack.pop()
        }
    }

    fun BlockNode.eval(): RuntimeValue {
        // additional scope because new variables can be declared in blocks of `if`, `while`, etc.
        // also, function parameters can be shadowed
        callStack.push(functionFullQualifiedName = null, callType = CallType.Block, callPosition = position)
        val result = try {
            statements.map { it.eval() as? RuntimeValue }.lastOrNull() ?: UnitValue
        } finally {
            callStack.pop()
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

    fun IntegerNode.eval() = IntValue(value)
    fun BooleanNode.eval() = BooleanValue(value)

    fun eval() = scriptNode.eval()

}
