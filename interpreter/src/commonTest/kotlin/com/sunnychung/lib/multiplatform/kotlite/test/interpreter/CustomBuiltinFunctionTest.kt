package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
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
                    "a" to "String",
                    "b" to "Int",
                    "c" to "String",
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
                parameterTypes = listOf("factor" to "Int"),
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
}
