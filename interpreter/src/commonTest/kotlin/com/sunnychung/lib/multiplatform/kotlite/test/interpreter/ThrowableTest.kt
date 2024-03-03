package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateRuntimeException
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
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

    @Test
    fun throwCustomException() {
        val interpreter = interpreter("""
            class UnexpectedException(message: String) : Throwable("${'$'}message error")
            var x = 1
            fun f(a: Int?) {
                val y: Int = a ?: throw UnexpectedException("some")
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
        assertEquals("UnexpectedException", error.error.clazz?.fullQualifiedName)
        assertTrue { error.stacktrace.isNotEmpty() }
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun tryCatchNPE() {
        val interpreter = interpreter("""
            var x = 1
            var z = 1
            var s = ""
            fun f(a: Int?) {
                val y: Int = a!!
                x += y
            }
            ++x
            try {
                f(null)
            } catch (e: NullPointerException) {
                ++z
                s += e.name
            }
            ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertEquals("NullPointerException", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun tryCatchCustomException() {
        val interpreter = interpreter("""
            class UnexpectedException : Throwable("some error")
            var x = 1
            var z = 1
            var s = ""
            fun f(a: Int?) {
                val y: Int = a ?: throw UnexpectedException()
                x += y
            }
            ++x
            try {
                f(null)
            } catch (e: UnexpectedException) {
                ++z
                s += e.name
            }
            ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertEquals("UnexpectedException", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun tryCatchExternalException() {
        val interpreter = interpreter("""
            var x = 1
            var z = 1
            var s = ""
            fun f(a: Int?) {
                val y: Int = (a ?: 1) / 0
                x += y
            }
            ++x
            try {
                f(null)
            } catch (e: Throwable) {
                ++z
                s += e.name
            }
            ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertTrue { (symbolTable.findPropertyByDeclaredName("s") as StringValue).value.contains("ArithmeticException") }
    }

    @Test
    fun tryCatchMultipleExceptions() {
        val interpreter = interpreter("""
            class UnexpectedException : Throwable("some error")
            var x = 1
            var z = 1
            var s = ""
            fun f(a: Int?) {
                val y: Int = a ?: throw UnexpectedException()
                x += y
            }
            ++x
            try {
                f(null)
            } catch (e: NullPointerException) {
                z += 6
                s += e.name
            } catch (e: UnexpectedException) {
                z += 11
                s += e.name
            } catch (e: Throwable) {
                z += 13
                s += e.name
            }
            ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertEquals("UnexpectedException", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun tryPreemptiveCatchMultipleExceptions() {
        val interpreter = interpreter("""
            class UnexpectedException : Throwable("some error")
            var x = 1
            var z = 1
            var s = ""
            fun f(a: Int?) {
                val y: Int = a ?: throw UnexpectedException()
                x += y
            }
            ++x
            try {
                f(null)
            } catch (e: NullPointerException) {
                z += 6
                s += e.name
            } catch (e: Throwable) {
                z += 13
                s += e.name
            } catch (e: UnexpectedException) {
                z += 11
                s += e.name
            }
            ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertEquals("UnexpectedException", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun tryFinally() {
        val interpreter = interpreter("""
            var x = 1
            ++x
            try {
                throw Throwable("some error")
            } finally {
                x += 10
            }
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
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun tryCatchFinally() {
        val interpreter = interpreter("""
            var x = 1
            ++x
            try {
                throw Throwable("some error")
            } catch (e: Throwable) {
                x += 7
            } finally {
                x += 10
            }
            ++x
            ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun tryCatchFinallyWithRethrow() {
        val interpreter = interpreter("""
            var x = 1
            ++x
            try {
                throw Throwable("some error")
            } catch (e: Throwable) {
                x += 7
                throw e
            } finally {
                x += 10
            }
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
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun emptyTryCatchFinally() {
        val interpreter = interpreter("""
            var x = 1
            ++x
            try {} catch (_: Throwable) {} finally {}
            ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun tryCatchExpression1() {
        val interpreter = interpreter("""
            val x: Int = try {
                12
            } catch (_: Throwable) {
                19
            } finally {
                "abc"
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun tryCatchExpression2() {
        val interpreter = interpreter("""
            val x: Int = try {
                throw Throwable()
                12
            } catch (_: Throwable) {
                19
            } finally {
                "abc"
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun tryCatchExpressionWithNothing() {
        val interpreter = interpreter("""
            val x: Int = try {
                12
            } catch (e: Throwable) {
                throw e
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun tryFinallyExpression() {
        val interpreter = interpreter("""
            val x: Int = try {
                12
            } finally {
                "abc"
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }
}
