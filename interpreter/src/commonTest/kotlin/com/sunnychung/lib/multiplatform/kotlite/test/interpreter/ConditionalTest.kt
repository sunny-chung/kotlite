package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ConditionalTest {

    @Test
    fun simpleIfTrue() {
        val interpreter = interpreter("""
            var x: Int = 11
            if (true) {
                x += 1
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun simpleIfFalse() {
        val interpreter = interpreter("""
            var x: Int = 11
            if (false) {
                x += 1
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun simpleIfStatementTrue() {
        val interpreter = interpreter("""
            var x: Int = 10
            if (true)
                x += 1
            x += 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun simpleIfStatementFalse() {
        val interpreter = interpreter("""
            var x: Int = 10
            if (false)
                x += 1
            x += 1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun simpleIfTrueElse() {
        val interpreter = interpreter("""
            var x: Int = 11
            if (true) {
                x += 1
            } else {
                x -= 1
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun simpleIfFalseElse() {
        val interpreter = interpreter("""
            var x: Int = 11
            if (false) {
                x += 1
            } else {
                x -= 1
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun simpleIfExpressionTrue() {
        val interpreter = interpreter("""
            var x: Int = 20
            x += if (true) {
                100
                10 + 2
            } else {
                200
                9
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(32, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun simpleIfExpressionFalse() {
        val interpreter = interpreter("""
            var x: Int = 20
            x += if (false) {
                100
                10 + 2
            } else {
                200
                9
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun ifTrueWithNewVariables() {
        val interpreter = interpreter("""
            var x: Int = 20
            x += if (true) {
                val y: Int = 10
                val z: Int = 2
                100
                y + z
            } else {
                val y: Int = 10
                val z: Int = 1
                200
                y - z
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(32, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun ifFalseWithNewVariables() {
        val interpreter = interpreter("""
            var x: Int = 20
            x += if (false) {
                val y: Int = 10
                val z: Int = 2
                100
                y + z
            } else {
                val y: Int = 10
                val z: Int = 1
                200
                y - z
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun ifTrueWithShadowedVariable() {
        val interpreter = interpreter("""
            var x: Int = 20
            x += if (true) {
                val x: Int = 10
                val z: Int = 2
                100
                x + z
            } else {
                val x: Int = 10
                val z: Int = 1
                200
                x - z
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(32, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun ifFalseWithShadowedVariable() {
        val interpreter = interpreter("""
            var x: Int = 20
            x += if (false) {
                val x: Int = 10
                val z: Int = 2
                100
                x + z
            } else {
                val x: Int = 10
                val z: Int = 1
                200
                x - z
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun intComparison() {
        val x = 20
        val y = 19
        val z = 20
        val testCases = listOf(
            "x > y" to (x > y),
            "x >= y" to (x >= y),
            "x < y" to (x < y),
            "x <= y" to (x <= y),
            "x == y" to (x == y),
            "x != y" to (x != y),
            "x > z" to (x > z),
            "x >= z" to (x >= z),
            "x < z" to (x < z),
            "x <= z" to (x <= z),
            "x == z" to (x == z),
            "x != z" to (x != z),
            "y > x" to (y > x),
            "y >= x" to (y >= x),
            "y < x" to (y < x),
            "y <= x" to (y <= x),
            "y == x" to (y == x),
            "y != x" to (y != x),
        )
        testCases.forEach {
            val interpreter = interpreter("""
                val x: Int = $x
                val y: Int = $y
                val z: Int = $z
                val r: Boolean = ${it.first}
            """.trimIndent())
            interpreter.eval()
            val symbolTable = interpreter.callStack.currentSymbolTable()
            println(symbolTable.propertyValues)
            assertEquals(4, symbolTable.propertyValues.size)
            assertEquals(it.second, (symbolTable.propertyValues["r"] as BooleanValue).value)
        }
    }

    @Test
    fun ifElseConditionTrue() {
        val interpreter = interpreter("""
            var x: Int = 20
            if (x >= 10) {
                x += 12
            } else {
                x += 9
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(32, (symbolTable.propertyValues["x"] as IntValue).value)
    }

    @Test
    fun ifElseConditionFalse() {
        val interpreter = interpreter("""
            var x: Int = 20
            if (x <= 10) {
                x += 12
            } else {
                x += 9
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.propertyValues["x"] as IntValue).value)
    }
}
