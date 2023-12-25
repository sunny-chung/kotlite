package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionTest {
    @Test
    fun simpleFunctionCall() {
        val interpreter = interpreter("""
            fun myFunction(a: Int, b: Int) {
                val q: Int = a + b
            }
            val x: Int = 1 + 2
            myFunction(3, 4)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
    }

    @Test
    fun simpleFunctionWithReturnValueCall() {
        val interpreter = interpreter("""
            fun myFunction(a: Int, b: Int): Int {
                val q: Int = a + b * 2
                return q + 1
            }
            val x: Int = 1 + 2
            x += myFunction(3, 4) + 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(16, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun simpleFunctionWithNamedArgument() {
        val interpreter = interpreter("""
            fun myFunction(a: Int, b: Int): Int {
                val q: Int = a + b * 2
                return q + 1
            }
            val x: Int = 1 + 2
            x += myFunction(b = 3, a = 4) + 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun functionShouldNotContinueExecutionAfterReturn() {
        val interpreter = interpreter("""
            fun myFunction(a: Int, b: Int): Int {
                val q: Int = a + b
                return q + 1
                q = 100
                return q - 1
            }
            val x: Int = 1 + 2
            x += myFunction(3, 4) + 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun conditionalReturn() {
        val interpreter = interpreter("""
            fun f(n: Int): Int {
                if (n < 10) return n
                return -1
            }
            val x: Int = f(5)
            val y: Int = f(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun recursion() {
        val interpreter = interpreter("""
            fun fib(n: Int): Int {
                if (n <= 1) return n
                return fib(n - 2) + fib(n - 1)
            }
            val x: Int = fib(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(55, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun nestedRecursion() {
        val interpreter = interpreter("""
            fun fib(n: Int): Int {
                fun f(n: Int) {
                    if (n <= 1) return n
                    return fib(n - 2) + fib(n - 1)
                }
                return if (n < 0) 0 else f(n)
            }
            val x: Int = fib(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(55, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun functionWithDefaultArgument() {
        val interpreter = interpreter("""
            fun myFunction(a: Int, b: Int = 100): Int {
                val q: Int = a + b * 2
                return q + 1
            }
            val x: Int = 1 + 2
            x += myFunction(a = 4) + 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(209, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun functionWithAllDefaultArguments() {
        val interpreter = interpreter("""
            fun myFunction(
                a: Int = 10,
                b: Int = 100,
            ): Int {
                val q: Int = a + b * 2
                return q + 1
            }
            val x: Int = 1 + 2
            x += myFunction()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(214, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun functionWithDefaultArgumentExpression() {
        val interpreter = interpreter("""
            fun myFunction(a: Int, b: Int = 100 + 2 * a): Int {
                val q: Int = a + b * 2
                return q + 1
            }
            val x: Int = 1 + 2
            x += myFunction(a = 4) + 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(225, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun functionWithOverridedDefaultArgumentExpression() {
        val interpreter = interpreter("""
            fun myFunction(a: Int, b: Int = 100 + 2 * a): Int {
                val q: Int = a + b * 2
                return q + 1
            }
            val x: Int = 1 + 2
            x += myFunction(a = 4, b = 5) + 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun functionVariableIsUnmodifiedAfterNewShadowVariableDeclaration() {
        val interpreter = interpreter("""
            val a: Int = 1
            val b: Int = if (a == 1) {
                fun myFunction(): Int {
                    return a
                }
                val a: Int = 2
                myFunction()
            } else -1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun functionVariablesAmongNestedRecursions() {
        val interpreter = interpreter("""
            var b: Int = 0
            fun f(x: Int): Int {
                var a: Int = 0
                fun g(x: Int) {
                    if (x <= 0) return
                    ++a
                    f(x - 1)
                }
                if (x <= 0) return 0
                ++b
                var y: Int = x
                while (y >= 0) {
                    g(y)
                    --y
                }
                return a + f(x - 1)
            }
            val r: Int = f(5)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(5 + 4 + 3 + 2 + 1, (symbolTable.findPropertyByDeclaredName("r") as IntValue).value)
        assertEquals(55, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}