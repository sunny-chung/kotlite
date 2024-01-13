package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateNullPointerException
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
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
    fun throwNPEOnNullAccessClassMemberProperty() {
        assertFailsWith<EvaluateNullPointerException> {
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
    fun throwNPEOnNullAccessClassMemberFunction() {
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
    fun throwNPEOnNullAccessClassExtensionFunction() {
        assertFailsWith<SemanticException> {
            interpreter("""
                class A
                fun A.f(): Int = 3
                val o: A? = null
                val x: Int = o.f()
        """.trimIndent()).eval()
        }
    }
}
