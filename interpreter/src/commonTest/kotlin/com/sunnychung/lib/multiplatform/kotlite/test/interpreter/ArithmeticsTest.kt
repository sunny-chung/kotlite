package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.NumberValue
import com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis.assertSemanticFail
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

    @Test
    fun longAddLong() {
        val interpreter = interpreter("""
            val a = 123L
            val b = 45L
            var x = 20L + 3000000000
            val y = 12L + 15
            val z = a + b
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(3000000020L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(27L, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
        assertEquals(168L, (symbolTable.findPropertyByDeclaredName("z") as LongValue).value)
    }

    @Test
    fun intAddLong() {
        val interpreter = interpreter("""
            val a = 123
            val b = 45L
            var x = 20 + 3000000000
            val y = 12L + 15
            val z = a + b
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(3000000020L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(27L, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
        assertEquals(168L, (symbolTable.findPropertyByDeclaredName("z") as LongValue).value)
    }

    @Test
    fun intMinusLong() {
        val interpreter = interpreter("""
            val a = 123
            val b = 45L
            var x = 20 - 3000000000
            val y = 12L - 15
            val z = a - b
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(-2999999980L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(-3L, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
        assertEquals(78L, (symbolTable.findPropertyByDeclaredName("z") as LongValue).value)
    }

    @Test
    fun intTimesLong() {
        val interpreter = interpreter("""
            val a = 123
            val b = 45L
            var x = 20 * 3000000000
            val y = 12L * 15
            val z = a * b
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(60000000000L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(180L, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
        assertEquals(5535L, (symbolTable.findPropertyByDeclaredName("z") as LongValue).value)
    }

    @Test
    fun intDivLong() {
        val interpreter = interpreter("""
            val a = 123
            val b = 45L
            var x = 3000000000 / 20
            val y = 12 / 15L
            val z = a / b
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(150000000L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(0L, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
        assertEquals(2L, (symbolTable.findPropertyByDeclaredName("z") as LongValue).value)
    }

    @Test
    fun intModLong() {
        val interpreter = interpreter("""
            val a = 123
            val b = 45L
            var x = 3000000000 % 20
            val y = 12 % 15L
            val z = a % b
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(0L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(12L, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
        assertEquals(33L, (symbolTable.findPropertyByDeclaredName("z") as LongValue).value)
    }

    @Test
    fun longPlusAssignLong() {
        val interpreter = interpreter("""
            var x = 3000000000L
            x += 1234567890123456L
            x += 2L
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(1234570890123458L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
    }

    @Test
    fun intPlusAssignLongShouldFail() {
        assertSemanticFail("""
            var a = 123
            a += 10L
        """.trimIndent())
    }

    @Test
    fun longPlusAssignInt() {
        val interpreter = interpreter("""
            var x = 3000000000L
            x += 10
            x += 2
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(3000000012L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
    }

    @Test
    fun longMinusAssignInt() {
        val interpreter = interpreter("""
            var x = 3000000000L
            x -= 10
            x -= 2
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(2999999988L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
    }

    @Test
    fun longTimesAssignInt() {
        val interpreter = interpreter("""
            var x = 3000000000L
            x *= 10
            x *= 2
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(60000000000L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
    }

    @Test
    fun longDivAssignInt() {
        val interpreter = interpreter("""
            var x = 3000000000L
            x /= 10
            x /= 2
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(150000000L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
    }

    @Test
    fun longModAssignInt() {
        val interpreter = interpreter("""
            var x = 3000000000L
            x %= 11
            x %= 3
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(2L, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
    }

    @Test
    fun prePlusPlusLong() {
        val interpreter = interpreter("""
            var x: Long = 20L
            val y: Long = ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
    }

    @Test
    fun postPlusPlusLong() {
        val interpreter = interpreter("""
            var x: Long = 20L
            val y: Long = x++
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
    }

    @Test
    fun preMinusMinusLong() {
        val interpreter = interpreter("""
            var x: Long = 20L
            val y: Long = --x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
    }

    @Test
    fun postMinusMinusLong() {
        val interpreter = interpreter("""
            var x: Long = 20L
            val y: Long = x--
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(19, (symbolTable.findPropertyByDeclaredName("x") as LongValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("y") as LongValue).value)
    }
}
