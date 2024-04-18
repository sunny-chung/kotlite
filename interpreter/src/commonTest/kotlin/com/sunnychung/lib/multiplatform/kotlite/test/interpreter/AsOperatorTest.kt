package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.error.EvaluateTypeCastException
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AsOperatorTest {

    @Test
    fun asOperatorSuccess() {
        val interpreter = interpreter("""
            fun f(x: Int): Any {
                return if (x > 0)
                    x
                else
                    "abc"
            }
            
            var x: Int = f(12) as Int
            var y: String = f(-12) as String
            var z: Int = (f(123) as Int) + 45
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("y") as StringValue).value)
        assertEquals(168, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
    }

    @Test
    fun asOperatorFail1() {
        val interpreter = interpreter("""
            fun f(x: Int): Any {
                return if (x > 0)
                    x
                else
                    "abc"
            }
            
            var x: Int = f(-12) as Int
            var y: String = f(-12) as String
        """.trimIndent())
        assertFailsWith<EvaluateTypeCastException> {
            interpreter.eval()
        }
    }

    @Test
    fun asOperatorFail2() {
        val interpreter = interpreter("""
            fun f(x: Int): Any {
                return if (x > 0)
                    x
                else
                    "abc"
            }
            
            var x: Int = f(12) as Int
            var y: String = f(12) as String
        """.trimIndent())
        assertFailsWith<EvaluateTypeCastException> {
            interpreter.eval()
        }
    }

    @Test
    fun nullableAsOperatorSuccess() {
        val interpreter = interpreter("""
            fun f(x: Int): Any {
                return if (x > 0)
                    x
                else
                    "abc"
            }
            
            var x: Int? = f(12) as? Int
            var y: String? = f(-12) as? String
            var z: Int? = (f(123) as? Int)!! + 45
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("y") as StringValue).value)
        assertEquals(168, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
    }

    @Test
    fun nullableAsOperatorReturnsNull() {
        val interpreter = interpreter("""
            fun f(x: Int): Any {
                return if (x > 0)
                    x
                else
                    "abc"
            }
            
            var x: Int? = f(-12) as? Int
            var y: String? = f(12) as? String
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("x") is NullValue)
        assertTrue(symbolTable.findPropertyByDeclaredName("y") is NullValue)
    }

    @Test
    fun asNullableType() {
        val interpreter = interpreter("""
            fun f(x: Int): Any? {
                return if (x > 100)
                    x
                else if (x > 0)
                    null
                else
                    "abc"
            }
            
            var x: Int? = f(12) as Int?
            var y: String = f(-12) as String
            var z: Int = (f(123) as Int) + 45
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("x") is NullValue)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("y") as StringValue).value)
        assertEquals(168, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
    }

    @Test
    fun asOperatorGenericTypeCastToAnyTypeArgument() {
        val interpreter = interpreter("""
            open class A
            open class B(val value: Int) : A()
            class Container<T>(val delegation: T)
            val o = Container<A>(B(100))
            val cast1: Container<A> = o as Container<A>
            val cast2: Container<B> = o as Container<B>
            val x = cast2.delegation.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(100, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }
}
