package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class OperatorFunctionTest {

    @Test
    fun operatorFunctionCanBeInvokedNormally() {
        val interpreter = interpreter("""
            class IntPair(val first: Int, val second: Int) {
                operator fun get(index: Int): Int {
                    return if (index == 0) {
                        first
                    } else if (index == 1) {
                        second
                    } else {
                        0
                    }
                }
            }
            val x = IntPair(123, 45)
            val a = x.get(0)
            val b = x.get(1)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun getOperatorSimple() {
        val interpreter = interpreter("""
            class IntPair(val first: Int, val second: Int) {
                operator fun get(index: Int): Int {
                    return if (index == 0) {
                        first
                    } else if (index == 1) {
                        second
                    } else {
                        0
                    }
                }
            }
            val x = IntPair(123, 45)
            val a = x[0]
            val b = x[1]
            val c = IntPair(67, 89)[0]
            val d = IntPair(23, 456)[1]
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(67, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(456, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun getOperatorWithGenericReturnType() {
        val interpreter = interpreter("""
            class MyPair<T>(val first: T, val second: T) {
                operator fun get(index: Int): T {
                    return if (index == 0) {
                        first
                    } else if (index == 1) {
                        second
                    } else {
                        first // TODO: throw exception
                    }
                }
            }
            val x = MyPair(123, 45)
            val y = MyPair("ab", "cde")
            val a = x[0]
            val b = x[1]
            val c = y[0]
            val d = y[1]
            val e = MyPair(true, false)[0]
            val f = MyPair(12.34, 56.7)[1]
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("ab", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("cde", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        compareNumber(56.7, symbolTable.findPropertyByDeclaredName("f") as DoubleValue)
    }

    @Test
    fun getOperatorWithTwoArguments() {
        val interpreter = interpreter("""
            class Sum {
                operator fun get(from: Int, to: Int): Int {
                    var sum = 0
                    var i = from
                    while (i <= to) {
                        sum += i
                        i++
                    }
                    return sum
                }
            }
            val sum = Sum()
            val a = sum[3, 6]
            val b = sum[12, 18]
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals((3..6).sum(), (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals((12..18).sum(), (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun indexAssignment() {
        val interpreter = interpreter("""
            var numSetCalls = 0
            var numValueCalls = 0
            class MyPair<T>(var first: T, var second: T) {
                operator fun get(index: Int): T {
                    return if (index == 0) {
                        first
                    } else if (index == 1) {
                        second
                    } else {
                        first // TODO: throw exception
                    }
                }
                operator fun set(index: Int, newValue: T) {
                    ++numSetCalls
                    if (index == 0) {
                        first = newValue
                    } else {
                        second = newValue
                    }
                }
            }
            fun <T> value(v: T): T {
                ++numValueCalls
                return v
            }
            val x = MyPair(123, 45)
            val a = x[0]
            val b = x[1]
            x[0] = value(67)
            x[1] = value(890)
            val c = x[0]
            val d = x[1]
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(67, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(890, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("numSetCalls") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("numValueCalls") as IntValue).value)
    }

    @Test
    fun indexPlusAssign() {
        val interpreter = interpreter("""
            var numSetCalls = 0
            var numValueCalls = 0
            class MyPair<T>(var first: T, var second: T) {
                operator fun get(index: Int): T {
                    return if (index == 0) {
                        first
                    } else if (index == 1) {
                        second
                    } else {
                        first // TODO: throw exception
                    }
                }
                operator fun set(index: Int, newValue: T) {
                    ++numSetCalls
                    if (index == 0) {
                        first = newValue
                    } else {
                        second = newValue
                    }
                }
            }
            fun <T> value(v: T): T {
                ++numValueCalls
                return v
            }
            val x = MyPair(123, 45)
            val y = MyPair("ab", "cde")
            
            val a = x[0]
            val b = x[1]
            x[0] += value(67)
            x[1] += value(890)
            x[1] -= value(12)
            val c = x[0]
            val d = x[1]
            
            val e = y[0]
            val f = y[1]
            y[0] += value("qwe")
            y[1] += value("rty")
            val g = y[0]
            val h = y[1]
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(12, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(190, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(923, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)

        assertEquals("ab", (symbolTable.findPropertyByDeclaredName("e") as StringValue).value)
        assertEquals("cde", (symbolTable.findPropertyByDeclaredName("f") as StringValue).value)
        assertEquals("abqwe", (symbolTable.findPropertyByDeclaredName("g") as StringValue).value)
        assertEquals("cderty", (symbolTable.findPropertyByDeclaredName("h") as StringValue).value)

        assertEquals(5, (symbolTable.findPropertyByDeclaredName("numSetCalls") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("numValueCalls") as IntValue).value)
    }

    @Test
    fun plusMinus() {
        val interpreter = interpreter("""
            class C(val value: Int)
            class B(val value: Int)
            class A(val value: Int) {
                operator fun plus(x: B): C {
                    return C(value + x.value * 2)
                }
            }
            operator fun C.minus(a: A): B {
                return B(value * 3 - a.value)
            }
            val c: C = A(7) + B(4)
            val cval = c.value
            val b: B = c - A(11)
            val bval = b.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("cval") as IntValue).value)
        assertEquals(34, (symbolTable.findPropertyByDeclaredName("bval") as IntValue).value)
    }

    @Test
    fun genericsTimesDivRem() {
        val interpreter = interpreter("""
            class C<T>(val value: Int)
            class B<T>(val value: Int)
            class A<T>(val value: Int) {
                operator fun times(x: B<T>): C<T> {
                    return C<T>(value + x.value * 2)
                }
            }
            operator fun <T> C<T>.rem(a: A<T>): B<T> {
                return B<T>(value * 3 - a.value)
            }
            operator fun <T> C<T>.div(b: B<T>): A<T> {
                return A<T>(value - b.value * 7)
            }
            val c: C<Int> = A<Int>(7) * B<Int>(4)
            val cval = c.value
            val b = c % A<Int>(11)
            val bval = b.value
            val a = c / b
            val aval: Int = a.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("cval") as IntValue).value)
        assertEquals(34, (symbolTable.findPropertyByDeclaredName("bval") as IntValue).value)
        assertEquals(-223, (symbolTable.findPropertyByDeclaredName("aval") as IntValue).value)
    }

    @Test
    fun timesAssignWithCustomTimesOperator() {
        val interpreter = interpreter("""
            class PowerCoefficient(val value: Int)
            operator fun Int.times(coefficient: PowerCoefficient): Int {
                var i = 0
                var result = 1
                while (++i <= coefficient.value) {
                    result *= this
                }
                return result
            }
            val a: Int = 5 * PowerCoefficient(4)
            val b = 7 * PowerCoefficient(5)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(625, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(16807, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun minusAssign() {
        val interpreter = interpreter("""
            class DoubledIntContainer(var value: Int) {
                operator fun minusAssign(other: Int) {
                    value -= other * 2
                }
            }
            val x = DoubledIntContainer(29)
            x -= 4
            val a: Int = x.value
            x -= 3
            val b: Int = x.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun genericsPlusAssign() {
        val interpreter = interpreter("""
            class NumberContainer<T>(var value: Int)
            operator fun <T> NumberContainer<T>.plusAssign(other: T) {
                value += other as Int
            }
            val x = NumberContainer<Int>(29)
            x += 3
            val a: Int = x.value
            x += 20
            val b: Int = x.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(32, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(52, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
