package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateRuntimeException
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ThrowableTest {
    @Test
    fun throwExceptionInGlobalShouldNotContinue() {
        val interpreter = interpreter("""
            var x = 1
            ++x
            throw Throwable("some error")
            ++x
            ++x
        """.trimIndent())
        val error = assertFailsWith<EvaluateRuntimeException> {
            interpreter.eval()
        }
        error.printWithStacktrace()
        assertEquals("some error", error.message)
        assertTrue { error.stacktrace.isNotEmpty() }
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun throwExceptionInFunctionShouldNotContinue() {
        val interpreter = interpreter("""
            var x = 1
            var y = 1
            fun f() {
                ++x
                ++y
                throw Throwable("some error")
                ++x
                ++y
            }
            ++x
            ++y
            f()
            ++x
            ++y
        """.trimIndent())
        val error = assertFailsWith<EvaluateRuntimeException> {
            interpreter.eval()
        }
        error.printWithStacktrace()
        assertEquals("some error", error.message)
        assertTrue { error.stacktrace.isNotEmpty() }
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun throwExceptionInClassFunctionShouldNotContinue() {
        val interpreter = interpreter("""
            var x = 1
            var y = 1
            class A {
                fun f() {
                    ++x
                    ++y
                    throw Throwable("some error")
                    ++x
                    ++y
                }
            }
            ++x
            ++y
            A().f()
            ++x
            ++y
        """.trimIndent())
        val error = assertFailsWith<EvaluateRuntimeException> {
            interpreter.eval()
        }
        error.printWithStacktrace()
        assertEquals("some error", error.message)
        assertTrue { error.stacktrace.isNotEmpty() }
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun throwExceptionInClassInitShouldNotContinue() {
        val interpreter = interpreter("""
            var x = 1
            var y = 1
            class A {
                init {
                    ++x
                    ++y
                    throw Throwable("some error")
                    ++x
                    ++y
                }
            }
            ++x
            ++y
            A()
            ++x
            ++y
        """.trimIndent())
        val error = assertFailsWith<EvaluateRuntimeException> {
            interpreter.eval()
        }
        error.printWithStacktrace()
        assertEquals("some error", error.message)
        assertTrue { error.stacktrace.isNotEmpty() }
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun throwInElvisExpression() {
        val interpreter = interpreter("""
            var x = 1
            fun f(a: Int?) {
                val y: Int = a ?: throw Throwable("some error")
                x += y
            }
            ++x
            f(null)
            ++x
        """.trimIndent())
        val error = assertFailsWith<EvaluateRuntimeException> {
            interpreter.eval()
        }
        error.printWithStacktrace()
        assertEquals("some error", error.message)
        assertTrue { error.stacktrace.isNotEmpty() }
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun notThrowInElvisExpressionWithThrowExpression() {
        val interpreter = interpreter("""
            var x = 1
            fun f(a: Int?) {
                val y: Int = a ?: throw Throwable("some error")
                x += y
            }
            ++x
            f(10)
            ++x
        """.trimIndent())
        interpreter.eval()

        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(13, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }
}
