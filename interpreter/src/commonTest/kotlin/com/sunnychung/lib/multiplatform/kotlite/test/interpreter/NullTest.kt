package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateNullPointerException
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
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

    @Test
    fun nullWithElvisOperator() {
        val interpreter = interpreter("""
            val x: Int = null ?: 123
            val y: String = null ?: "abc"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("y") as StringValue).value)
    }

    @Test
    fun nonNullWithElvisOperator() {
        val interpreter = interpreter("""
            val x: Int = 456 ?: 123
            val y: String = "def" ?: "abc"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(456, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("y") as StringValue).value)
    }

    @Test
    fun nullableElvisOperator() {
        val interpreter = interpreter("""
            fun f(x: Int) = if (x > 0) x else null
            val x: Int? = f(123) ?: null
            val y: Int? = f(-123) ?: null
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("y"))
    }

    @Test
    fun elvisOperatorWithSuperTypes() {
        val interpreter = interpreter("""
            open class A
            class B : A()
            class C : A()
            fun <T> f(x: Int, o: T) = if (x > 0) o else null
            val x: A = f(123, B()) ?: C()
            val y: A = f(-123, B()) ?: C()
            
            val x0 = x is A
            val x1 = x is B
            val x2 = x is C
            val y0 = y is A
            val y1 = y is B
            val y2 = y is C
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("x0") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("x1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("x2") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("y0") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("y1") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("y2") as BooleanValue).value)
    }

    @Test
    fun chainedElvisOperators() {
        val interpreter = interpreter("""
            fun <T> f(x: Int, o: T) = if (x > 0) o else null
            val a: Int? = f(-123, 456) ?: f(-123, 567) ?: f(-123, 678)
            val b: Int? = f(-123, 456) ?: f(-123, 567) ?: f(123, 678)
            val c: Int? = f(-123, 456) ?: f(123, 567) ?: f(123, 678)
            val d: Int? = f(123, 456) ?: f(123, 567) ?: f(-123, 678)
            val e: Int = f(123, 456) ?: f(123, 567) ?: 678
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("a"))
        assertEquals(678, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(567, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(456, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(456, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun accessMemberOfNull() {
        val interpreter = interpreter("""
            class A {
                fun f(): Int = 3
            }
            fun f(x: Int) = if (x > 0) A() else null
            val x: Int = f(-1)?.f() ?: -5
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(-5, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }
}
