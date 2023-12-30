package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PropertyImmutableTest {

    @Test
    fun outerScopeSuccess1() {
        val interpreter = interpreter("""
            val a: Int = 3
            var b: Int = 4
            b = 5
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun outerScopeSuccess2() {
        val interpreter = interpreter("""
            val a: Int
            var b: Int
            b = 4
            a = 3
            b = 5
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun outerScopeFail1() {
        val interpreter = interpreter("""
            val a: Int = 3
            var b: Int = 4
            b = 5
            a = 6
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun outerScopeFail2() {
        val interpreter = interpreter("""
            val a: Int
            var b: Int
            b = 4
            a = 3
            b = 5
            a = 6
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun blockFail1() {
        val interpreter = interpreter("""
            if (true) {
                val a: Int = 2
                a = 30
            }
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun blockFail2() {
        val interpreter = interpreter("""
            val a: Int = 2
            if (true) {
                a = 30
            }
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun functionArgumentFail() {
        val interpreter = interpreter("""
            fun f(a: Int = 20) {
                a = 30
            }
            f()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun functionModifyOutsideFail() {
        val interpreter = interpreter("""
            val a: Int = 2
            fun f() {
                a = 30
            }
            f()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classPrimaryConstructorFail1() {
        val interpreter = interpreter("""
            class Cls(c: Int = 10, var a: Int = 60, var b: Int = c++)
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classPrimaryConstructorFail2() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60, var b: Int = a++)
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classInitFail1() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                val c: Int = ++b
            }
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classInitFail2() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                init {
                    b = 20
                }
            }
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classFunctionArgument() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                fun f(c: Int) {
                    c = 20
                }
            }
            Cls().f(1)
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classFunction() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                fun f() {
                    b = 20
                }
            }
            Cls().f()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }
}
