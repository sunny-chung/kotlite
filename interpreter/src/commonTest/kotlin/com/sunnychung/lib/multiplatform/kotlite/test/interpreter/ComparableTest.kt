package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ComparableTest {

    @Test
    fun extensionFunction() {
        val interpreter = interpreter("""
            fun <T> Comparable<T>.isSmallerThan(other: Comparable<T>): Boolean {
                return compareTo(other) < 0
            }
            val a = 3.isSmallerThan(5)
            val b = 5.isSmallerThan(3)
            val c = 3.isSmallerThan(3)
            
            val d = 3.6.isSmallerThan(4.1)
            val e = 5.2.isSmallerThan(1.4)
            
            val f = "ab".isSmallerThan("cdef")
            val g = "cdef".isSmallerThan("ab")
            val h = "ghijk".isSmallerThan("ghijk")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
    }

    @Test
    fun customComparisonOperator() {
        val operators = listOf(
            "<" to { a: Int, b: Int -> a < b},
            "<=" to { a: Int, b: Int -> a <= b},
            ">" to { a: Int, b: Int -> a > b},
            ">=" to { a: Int, b: Int -> a >= b},
        )
        (1 .. 3).forEach { a ->
            (1 .. 3).forEach { b ->
                operators.forEach { op ->
                    val interpreter = interpreter("""
                        class Num(val value: Int) {
                            operator fun compareTo(other: Num): Int = value.compareTo(other.value)
                        }
                        val x = Num($a)
                        val y = Num($b)
                        val a: Boolean = x ${op.first} y
                    """.trimIndent())
                    interpreter.eval()
                    val symbolTable = interpreter.callStack.currentSymbolTable()
                    println(symbolTable.propertyValues)
                    assertEquals(3, symbolTable.propertyValues.size)
                    assertEquals(op.second(a, b), (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
                }
            }
        }
    }

    @Test
    fun inheritedComparison() {
        val interpreter = interpreter("""
            open class Num(val value: Int) {
                operator fun compareTo(other: Num): Int = value.compareTo(other.value)
            }
            open class A(value: Int) : Num(value)
            open class B(value: Int) : A(value)
            class C(value: Int) : B(value)
            val a: Boolean = C(-5) < C(-8)
            val b: Boolean = C(-5) < C(7)
            val c: Boolean = C(7) >= C(7)
            val d: Boolean = C(16) > C(7)
            val e: Boolean = C(16) <= C(13)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
    }

    @Test
    fun inheritedGenericComparison() {
        val interpreter = interpreter("""
            open class Num<P : Comparable<P>>(val value: P) {
                operator fun compareTo(other: Num<P>): Int = value.compareTo(other.value)
            }
            open class A<X : Comparable<X>>(value: X) : Num<X>(value)
            open class B<X : Comparable<X>>(value: X) : A<X>(value)
            class C<T : Comparable<T>>(value: T) : B<T>(value)
            val a: Boolean = C(-5) < C(-8)
            val b: Boolean = C(-5) < C(7)
            val c: Boolean = C(7) >= C(7)
            val d: Boolean = C(16) > C(7)
            val e: Boolean = C(16) <= C(13)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
    }
}
