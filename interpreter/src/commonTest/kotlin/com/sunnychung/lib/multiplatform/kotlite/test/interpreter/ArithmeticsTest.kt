package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NumberValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun compareNumber(expected: Number, actual: NumberValue<*>) {
    // check both because the expression `1.2345 is Int` in JS evaluates to true
    if (expected is Int && actual is IntValue) {
        assertEquals(expected, (actual as IntValue).value)
    } else {
        expected as Double
        assertTrue((actual as DoubleValue).value in (expected - 0.0001 .. expected + 0.0001))
    }
}

class ArithmeticsTest {

    @Test
    fun simpleAssignmentWithArithmetics() {
        val interpreter = interpreter("""
            val x: Int = 1 + 2
            val y: Int = 5 + 4 * (7 + 3) - 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(44, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun simpleAssignmentWithArithmeticsAndVariableReferences() {
        val interpreter = interpreter("""
            val x: Int = 1 + 2
            val y: Int = 5 + 4 * (7 + 3) - 1
            val z: Int = x * 2 + y
            var a: Int = z
            a -= y - 4
            a %= x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(44, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
        assertEquals(50, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun prePlusPlusInt() {
        val interpreter = interpreter("""
            var x: Int = 20
            val y: Int = ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun postPlusPlusInt() {
        val interpreter = interpreter("""
            var x: Int = 20
            val y: Int = x++
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun preMinusMinusInt() {
        val interpreter = interpreter("""
            var x: Int = 20
            val y: Int = --x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun postMinusMinusInt() {
        val interpreter = interpreter("""
            var x: Int = 20
            val y: Int = x--
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun intAddDouble() {
        val interpreter = interpreter("""
            val a: Int = 3
            val b: Double = 0.5
            val x: Double = a + b
            val y: Double = b + a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        compareNumber(3.5, symbolTable.findPropertyByDeclaredName("x") as NumberValue<*>)
        compareNumber(3.5, symbolTable.findPropertyByDeclaredName("y") as NumberValue<*>)
    }

    @Test
    fun intMinusDouble() {
        val interpreter = interpreter("""
            val a: Int = 3
            val b: Double = 0.5
            val x: Double = a - b
            val y: Double = b - a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        compareNumber(2.5, symbolTable.findPropertyByDeclaredName("x") as NumberValue<*>)
        compareNumber(-2.5, symbolTable.findPropertyByDeclaredName("y") as NumberValue<*>)
    }

    @Test
    fun intTimesDouble() {
        val interpreter = interpreter("""
            val a: Int = 3
            val b: Double = 0.5
            val x: Double = a * b
            val y: Double = b * a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        compareNumber(1.5, symbolTable.findPropertyByDeclaredName("x") as NumberValue<*>)
        compareNumber(1.5, symbolTable.findPropertyByDeclaredName("y") as NumberValue<*>)
    }

    @Test
    fun intDivDouble() {
        val interpreter = interpreter("""
            val a: Int = 3
            val b: Double = 0.5
            val x: Double = a / b
            val y: Double = b / a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        compareNumber(6.0, symbolTable.findPropertyByDeclaredName("x") as NumberValue<*>)
        compareNumber(0.5 / 3, symbolTable.findPropertyByDeclaredName("y") as NumberValue<*>)
    }

    @Test
    fun intModDouble() {
        val interpreter = interpreter("""
            val a: Int = 3
            val b: Double = 0.5
            val x: Double = a % b
            val y: Double = b % a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        compareNumber(0.0, symbolTable.findPropertyByDeclaredName("x") as NumberValue<*>)
        compareNumber(0.5, symbolTable.findPropertyByDeclaredName("y") as NumberValue<*>)
    }

    @Test
    fun mixedIntDoubleCalculation() {
        val interpreter = interpreter("""
            val a: Int = 3
            val b: Double = 0.5
            val x: Double = 2 * a * 1.2 / 4 - 7 + 5.6 % 2 + -b - 2.0 * (-1.1 + (0.2 / 2))
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        compareNumber(-2.1, symbolTable.findPropertyByDeclaredName("x") as NumberValue<*>)
    }

    @Test
    fun prePlusPlusDouble() {
        val interpreter = interpreter("""
            var x: Double = 4.6
            val y: Double = ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        compareNumber(5.6, symbolTable.findPropertyByDeclaredName("x") as DoubleValue)
        compareNumber(5.6, symbolTable.findPropertyByDeclaredName("y") as DoubleValue)
    }

    @Test
    fun postPlusPlusDouble() {
        val interpreter = interpreter("""
            var x: Double = 4.6
            val y: Double = x++
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        compareNumber(5.6, symbolTable.findPropertyByDeclaredName("x") as DoubleValue)
        compareNumber(4.6, symbolTable.findPropertyByDeclaredName("y") as DoubleValue)
    }

    @Test
    fun preMinusMinusDouble() {
        val interpreter = interpreter("""
            var x: Double = 4.6
            val y: Double = --x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        compareNumber(3.6, symbolTable.findPropertyByDeclaredName("x") as DoubleValue)
        compareNumber(3.6, symbolTable.findPropertyByDeclaredName("y") as DoubleValue)
    }

    @Test
    fun postMinusMinusDouble() {
        val interpreter = interpreter("""
            var x: Double = 4.6
            val y: Double = x--
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        compareNumber(3.6, symbolTable.findPropertyByDeclaredName("x") as DoubleValue)
        compareNumber(4.6, symbolTable.findPropertyByDeclaredName("y") as DoubleValue)
    }

    @Test
    fun plusAssignDouble() {
        val interpreter = interpreter("""
            var x: Double = 2.9
            x -= 0.9
            x /= 0.5
            x *= 3.0 / 2.0
            x %= 4.91
            x += 2.345
            x -= 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        compareNumber(2.435, symbolTable.findPropertyByDeclaredName("x") as DoubleValue)
    }
}