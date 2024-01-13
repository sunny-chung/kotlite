package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateNullPointerException
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NullTest {

    @Test
    fun compareNullWithInt() {
        val interpreter = interpreter("""
            val x: Int = 10
            val a: Boolean = x == null
            val b: Boolean = x != null
            val c: Boolean = null == x
            val d: Boolean = null != x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }

    @Test
    fun compareNullWithIntNullable1() {
        val interpreter = interpreter("""
            val x: Int? = 10
            val a: Boolean = x == null
            val b: Boolean = x != null
            val c: Boolean = null == x
            val d: Boolean = null != x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }

    @Test
    fun compareNullWithIntNullable2() {
        val interpreter = interpreter("""
            val x: Int? = null
            val a: Boolean = x == null
            val b: Boolean = x != null
            val c: Boolean = null == x
            val d: Boolean = null != x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }

    @Test
    fun preventNullAccessClassMemberProperty() {
        assertFailsWith<SemanticException> {
            interpreter("""
                class A {
                    val a: Int = 3
                }
                val o: A? = null
                val x: Int = o.a
            """.trimIndent()).eval()
        }
    }

    @Test
    fun preventNullAccessClassMemberFunction() {
        assertFailsWith<SemanticException> {
            interpreter("""
                class A {
                    fun f(): Int = 3
                }
                val o: A? = null
                val x: Int = o.f()
            """.trimIndent()).eval()
        }
    }

    @Test
    fun preventNullAccessClassExtensionFunction() {
        assertFailsWith<SemanticException> {
            interpreter("""
                class A
                fun A.f(): Int = 3
                val o: A? = null
                val x: Int = o.f()
            """.trimIndent()).eval()
        }
    }

    @Test
    fun throwNPEOnNonNullAssert1() {
        assertFailsWith<EvaluateNullPointerException> {
            interpreter("""
                class A
                val o: A? = null
                val x = o!!
            """.trimIndent()).eval()
        }
    }

    @Test
    fun throwNPEOnNonNullAssert2() {
        assertFailsWith<EvaluateNullPointerException> {
            interpreter("""
                class A
                fun f(): A? = null
                val x = f()!!
            """.trimIndent()).eval()
        }
    }

    @Test
    fun throwNPEOnNonNullAssert3() {
        assertFailsWith<EvaluateNullPointerException> {
            interpreter("""
                val x = null!!
            """.trimIndent()).eval()
        }
    }

    @Test
    fun nullAssertThenCallClassMemberProperty() {
        val interpreter = interpreter("""
            class A {
                val a: Int = 3
            }
            val o: A? = A()
            val x: Int = o!!.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun nullAssertThenCallClassMemberFunction() {
        val interpreter = interpreter("""
            class A {
                fun f(): Int = 3
            }
            val o: A? = A()
            val x: Int = o!!.f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun nullAssertThenCallClassExtensionFunction() {
        val interpreter = interpreter("""
            class A
            fun A.f(): Int = 3
            val o: A? = A()
            val x: Int = o!!.f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }
}
