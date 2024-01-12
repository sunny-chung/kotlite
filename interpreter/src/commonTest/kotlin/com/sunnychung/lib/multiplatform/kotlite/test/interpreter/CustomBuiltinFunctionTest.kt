package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomBuiltinFunctionTest {

    @Test
    fun customGlobalFunction() {
        val env = ExecutionEnvironment().apply {
            registerFunction(CustomFunctionDefinition(
                receiverType = null,
                functionName = "myGlobalFunc",
                returnType = "String",
                parameterTypes = listOf(
                    CustomFunctionParameter("a", "String"),
                    CustomFunctionParameter("b", "Int"),
                    CustomFunctionParameter("c", "String"),
                ),
                executable = { _, args ->
                    val s = StringValue("${(args[0] as StringValue).value}|${(args[1] as IntValue).value + 100}|${(args[2] as StringValue).value}")
                    println(s.value)
                    s
                }
            ))
        }
        val interpreter = interpreter("""
            val a = myGlobalFunc(c = "end", a = "start", b = 20)
            val b = myGlobalFunc("aaaaa", 123, "b")
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("start|120|end", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("aaaaa|223|b", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun customExtensionFunction() {
        val env = ExecutionEnvironment().apply {
            registerFunction(CustomFunctionDefinition(
                receiverType = "String",
                functionName = "size",
                returnType = "Int",
                parameterTypes = listOf(CustomFunctionParameter("factor", "Int")),
                executable = { receiver, args ->
                    IntValue((receiver as StringValue).value.length * (args[0] as IntValue).value)
                }
            ))
        }
        val interpreter = interpreter("""
            val a = "abcdefghijk"
            val b = a.size(2)
            val c = "abcd".size(3)
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun optionalParameter() {
        val env = ExecutionEnvironment().apply {
            registerFunction(CustomFunctionDefinition(
                receiverType = "String",
                functionName = "size",
                returnType = "Int",
                parameterTypes = listOf(CustomFunctionParameter("factor", "Int", defaultValueExpression = "1")),
                executable = { receiver, args ->
                    IntValue((receiver as StringValue).value.length * (args[0] as IntValue).value)
                }
            ))
        }
        val interpreter = interpreter("""
            val a = "abcdefghijk"
            val b = a.size(2)
            val c = "abcd".size()
            val d = a.size()
            val e = "abcd".size(3)
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun optionalParameterExpression() {
        val env = ExecutionEnvironment().apply {
            registerFunction(CustomFunctionDefinition(
                receiverType = "Int",
                functionName = "myfunc",
                returnType = "Int",
                parameterTypes = listOf(
                    CustomFunctionParameter("a", "Int"),
                    CustomFunctionParameter("b", "Int"),
                    CustomFunctionParameter("c", "Int", defaultValueExpression = "a + b + this")
                ),
                executable = { receiver, args ->
                    IntValue(((args[0] as IntValue).value + (args[1] as IntValue).value) * (args[2] as IntValue).value)
                }
            ))
        }
        val interpreter = interpreter("""
            val a = 0.myfunc(3, 4)
            val b = 0.myfunc(3, 4, 3)
            val c = 0.myfunc(5, -1)
            val d = 2.myfunc(5, -1)
            val e = 5.myfunc(5, 3)
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(49, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(16, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(104, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun customGlobalFunctionOverload() {
        val env = ExecutionEnvironment().apply {
            registerFunction(CustomFunctionDefinition(
                receiverType = null,
                functionName = "myGlobalFunc",
                returnType = "String",
                parameterTypes = listOf(
                    CustomFunctionParameter("a", "String"),
                    CustomFunctionParameter("b", "Int"),
                    CustomFunctionParameter("c", "String"),
                ),
                executable = { _, args ->
                    val s = StringValue("A|${(args[0] as StringValue).value}|${(args[1] as IntValue).value + 100}|${(args[2] as StringValue).value}")
                    s
                }
            ))
            registerFunction(CustomFunctionDefinition(
                receiverType = null,
                functionName = "myGlobalFunc",
                returnType = "String",
                parameterTypes = listOf(
                    CustomFunctionParameter("a", "String"),
                    CustomFunctionParameter("b", "String"),
                    CustomFunctionParameter("c", "String"),
                ),
                executable = { _, args ->
                    val s = StringValue("B|${(args[0] as StringValue).value}|${(args[1] as StringValue).value}|${(args[2] as StringValue).value}")
                    s
                }
            ))
        }
        val interpreter = interpreter("""
            val a = myGlobalFunc(c = "end", a = "start", b = "20")
            val b = myGlobalFunc("aaaaa", 123, "b")
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("B|start|20|end", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("A|aaaaa|223|b", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun customExtensionFunctionOverload() {
        val env = ExecutionEnvironment().apply {
            registerFunction(CustomFunctionDefinition(
                receiverType = "String",
                functionName = "size",
                returnType = "Int",
                parameterTypes = listOf(CustomFunctionParameter("factor", "Int")),
                executable = { receiver, args ->
                    IntValue((receiver as StringValue).value.length * (args[0] as IntValue).value)
                }
            ))
            registerFunction(CustomFunctionDefinition(
                receiverType = "String",
                functionName = "size",
                returnType = "Int",
                parameterTypes = listOf(CustomFunctionParameter("factor", "Int"), CustomFunctionParameter("factor2", "Int")),
                executable = { receiver, args ->
                    IntValue((receiver as StringValue).value.length * ((args[0] as IntValue).value + (args[1] as IntValue).value))
                }
            ))
        }
        val interpreter = interpreter("""
            val a = "abcdefghijk"
            val b = a.size(2, 1)
            val c = "abcd".size(3, 1)
            val d = a.size(2)
            val e = "abcd".size(3)
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(33, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(16, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }
}
