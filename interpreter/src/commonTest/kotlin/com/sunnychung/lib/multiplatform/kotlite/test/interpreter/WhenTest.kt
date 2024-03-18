package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import kotlin.test.Test
import kotlin.test.assertEquals

class WhenTest {

    @Test
    fun equality() {
        val interpreter = interpreter("""
            fun f(x: Int): Int = when (x) {
                1 -> 15
                2 -> -3
                3 -> 6
                else -> -1
            }
            val a: Int = f(1)
            val b: Int = f(2)
            val c: Int = f(3)
            val d: Int = f(4)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun multipleEquality() {
        val interpreter = interpreter("""
            fun f(x: Int): Int = when (x) {
                1, 3 -> 15
                5, 2, 1 -> -3
                4 -> 6
                else -> -1
            }
            val a: Int = f(1)
            val b: Int = f(2)
            val c: Int = f(3)
            val d: Int = f(4)
            val e: Int = f(5)
            val f: Int = f(6)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun typeCheck() {
        val interpreter = interpreter("""
            open class A
            open class B : A()
            class C : A()
            class D : B()
            class Other
            fun f(x: Any): Int = when (x) {
                is C -> 15
                is B -> -3
                is A -> 6
                else -> -1
            }
            val a: Int = f(A())
            val b: Int = f(B())
            val c: Int = f(C())
            val d: Int = f(D())
            val e: Int = f(Other())
            val f: Int = f(6)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun multipleTypeCheck() {
        val interpreter = interpreter("""
            open class A
            open class B : A()
            class C : A()
            class D : B()
            class Other
            fun f(x: Any): Int = when (x) {
                is C, is D -> 15
                is B -> -3
                is A -> 6
                else -> -1
            }
            val a: Int = f(A())
            val b: Int = f(B())
            val c: Int = f(C())
            val d: Int = f(D())
            val e: Int = f(Other())
            val f: Int = f(6)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun mixTypeCheckAndEquality() {
        val interpreter = interpreter("""
            fun f(x: Any): Int = when (x) {
                is Double, 3, true -> 15
                1 -> -3
                is Int -> 6
                else -> -1
            }
            val a: Int = f(1)
            val b: Int = f(2)
            val c: Int = f(3)
            val d: Int = f(3.5)
            val e: Int = f(true)
            val f: Int = f(false)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun noSubject() {
        val interpreter = interpreter("""
            fun f(x: Int): Int = when {
                x >= 0 && x < 5 -> 15
                x < 20 -> -3
                x < 100 || x > 1000 -> 6
                else -> -1
            }
            val a: Int = f(1)
            val b: Int = f(10)
            val c: Int = f(30)
            val d: Int = f(100)
            val e: Int = f(-10)
            val f: Int = f(10000)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun subjectAsVal() {
        val interpreter = interpreter("""
            fun f(x: Int): Int = when (val y = x * 10) {
                10 -> 15
                20 -> -3
                30 -> 6
                else -> {
                    val z = y
                    z * y
                }
            }
            val a: Int = f(1)
            val b: Int = f(2)
            val c: Int = f(3)
            val d: Int = f(4)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-3, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(1600, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun multipleReturnType() {
        val interpreter = interpreter("""
            open class A
            open class B : A()
            class C : A()
            class D : B()
            class Other
            fun f(x: Int) = when (x) {
                1, 2, 4 -> B()
                5 -> D()
                else -> B()
            }
            val a: B = f(1)
            val b: B = f(3)
            val c: B = f(5)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("B", (symbolTable.findPropertyByDeclaredName("a") as ClassInstance).clazz!!.fullQualifiedName)
        assertEquals("B", (symbolTable.findPropertyByDeclaredName("b") as ClassInstance).clazz!!.fullQualifiedName)
        assertEquals("D", (symbolTable.findPropertyByDeclaredName("c") as ClassInstance).clazz!!.fullQualifiedName)
    }

    @Test
    fun multipleReturnTypeWithThrow() {
        val interpreter = interpreter("""
            open class A
            open class B : A()
            class C : A()
            class D : B()
            class Other
            class MyException : Exception("error")
            fun f(x: Int) = when (x) {
                1, 2, 4 -> throw MyException()
                5 -> D()
                else -> throw MyException()
            }
            fun g(x: Int): B? = try {
                f(x)
            } catch (_: MyException) {
                null
            }
            val a: B? = g(1)
            val b: B? = g(3)
            val c: B = f(5)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("a"))
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("b"))
        assertEquals("D", (symbolTable.findPropertyByDeclaredName("c") as ClassInstance).clazz!!.fullQualifiedName)
    }

    @Test
    fun rangeTest() {
        val interpreter = interpreter("""
            fun <T: Comparable<T>> makeList(vararg values: T): List<T> = values
            operator fun List<Int>.contains(x: Int): Boolean {
                for (it in this) {
                    if (it == x) {
                        return true
                    }
                }
                return false
            }
            class MyRange<T : Comparable<T>>(val from: T, val to: T) {
                operator fun contains(a: T): Boolean {
                    return from <= a && a <= to
                }
            }
            fun f(x: Int): Int {
                var x = x - 1
                return when (++x) { // test for the "subject" is only evaluated once
                    in makeList(1, 4, 5, 8) -> 21
                    in MyRange(from = 5, to = 7) -> 22
                    in makeList(2, 16) -> 23
                    in MyRange(14, 50) -> 24
                    66 -> 25
                    else -> 26
                }
            }
            val a: Int = f(8)
            val b: Int = f(5)
            val c: Int = f(7)
            val d: Int = f(3)
            val e: Int = f(16)
            val f: Int = f(29)
            val g: Int = f(51)
            val h: Int = f(66)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(26, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(23, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(26, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("h") as IntValue).value)
    }
}
