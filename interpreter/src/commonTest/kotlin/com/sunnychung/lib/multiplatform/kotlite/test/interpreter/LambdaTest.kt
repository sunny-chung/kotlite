package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class LambdaTest {
    @Test
    fun lambdaSameType() {
        val interpreter = interpreter("""
            val f: (Int) -> Int = { i: Int ->
                i * 2
            }
            val x: Int = f(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun lambdaMultipleInputs() {
        val interpreter = interpreter("""
            val f: (Int, Int) -> Int = { a: Int, b: Int ->
                a + b
            }
            val x: Int = f(10, 19)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun lambdaTransformType() {
        val interpreter = interpreter("""
            val f: (Int) -> String = { i: Int ->
                "<${'$'}{i * 2}>"
            }
            val x: String = f(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("<20>", (symbolTable.findPropertyByDeclaredName("x") as StringValue).value)
    }

    @Test
    fun lambdaNoInput() {
        val interpreter = interpreter("""
            val f: () -> String = {
                var i: Int = 10
                var s: String = ""
                while (i > 0) {
                    s += i
                    i--
                }
                s
            }
            val x: String = f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("10987654321", (symbolTable.findPropertyByDeclaredName("x") as StringValue).value)
    }

    @Test
    fun lambdaNoOutput() {
        val interpreter = interpreter("""
            var x: Int = 10
            val f: (Int) -> Unit = { i: Int ->
                x += 2 * i
            }
            f(5)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun lambdaNoInputNoOutput() {
        val interpreter = interpreter("""
            var x: Int = 10
            val f: () -> Unit = {
                x *= 2
            }
            f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun reassignLambda() {
        val interpreter = interpreter("""
            var x: Int = 10
            var f: (Int) -> Int = { x: Int ->
                x + 1
            }
            if (x > 5) {
                f = { i: Int -> i + i / 2 - 1 }
            } else {
                f = { x: Int -> x * 2 }
            }
            x = f(2 * x)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun functionHasHigherPrecedenceThanLambda() {
        val interpreter = interpreter("""
            var x: Int = 10
            val f: (Int) -> Int = { x: Int ->
                x + 1
            }
            fun f(x: Int): Int {
                return x + 9
            }
            x = f(2 * x)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }
}