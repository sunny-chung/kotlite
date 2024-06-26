package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class InterfaceTest {
    @Test
    fun invokeFunction() {
        val interpreter = interpreter("""
            interface I {
                fun f(x: Int): Int
            }
            class A : I {
                override fun f(x: Int) = 2 * x
            }
            class B : I {
                override fun f(x: Int) = 3 * x
            }
            fun getInstance(x: Int): I {
                return if (x > 0) {
                    A()
                } else {
                    B()
                }
            }
            val a = getInstance(1).f(12)
            val b = getInstance(-1).f(12)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(36, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunction1() {
        val interpreter = interpreter("""
            interface I {
                fun f(x: Int): Int
            }
            class A : I {
                override fun f(x: Int) = 2 * x
            }
            class B : I {
                override fun f(x: Int) = 3 * x
            }
            fun I.g(x: Int) = f(x) + 1 
            fun getInstance(x: Int): I {
                return if (x > 0) {
                    A()
                } else {
                    B()
                }
            }
            val a = getInstance(1).g(12)
            val b = getInstance(-1).g(12)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(37, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunction2() {
        val interpreter = interpreter("""
            interface I {
                fun f(x: Int): Int
            }
            class A : I {
                override fun f(x: Int) = 2 * x
            }
            class B : I {
                override fun f(x: Int) = 3 * x
            }
            fun I.g(x: Int) = f(x) + 1
            val a = A().g(12)
            val b = B().g(12)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(37, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun invokeInheritedFunction() {
        val interpreter = interpreter("""
            interface I {
                fun f(x: Int): Int
            }
            abstract class Base : I
            class A : Base() {
                override fun f(x: Int) = 2 * x
            }
            class B : Base() {
                override fun f(x: Int) = 3 * x
            }
            fun getInstance(x: Int): I {
                return if (x > 0) {
                    A()
                } else {
                    B()
                }
            }
            val a = getInstance(1).f(12)
            val b = getInstance(-1).f(12)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(36, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun implementMultipleInterfaces() {
        val interpreter = interpreter("""
            interface I {
                fun f(x: Int): Int
            }
            interface J {
                fun g(): Int
            }
            interface K : J {
                fun h(): Int
            }
            class A : I, K {
                override fun f(x: Int) = 2 * x
                override fun g() = 20
                override fun h() = 29
            }
            val o: K = A()
            val a = (o as I).f(12)
            val b = o.g()
            val c = o.h()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun implementGenericInterfaces() {
        val interpreter = interpreter("""
            interface I<T> {
                fun f(x: T): T
            }
            interface J<A> {
                fun g(): A
            }
            interface K<A> : J<A> {
                fun h(): A
            }
            class A : I<Int>, K<Int> {
                override fun f(x: Int) = 2 * x
                override fun g() = 20
                override fun h() = 29
            }
            val o: K<Int> = A()
            val a = (o as I<Int>).f(12)
            val b = (o as A).f(12)
            val c = o.g()
            val d = o.h()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }
}
