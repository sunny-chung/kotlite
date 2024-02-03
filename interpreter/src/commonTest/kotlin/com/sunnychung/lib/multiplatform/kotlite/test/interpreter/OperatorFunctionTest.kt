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
}
