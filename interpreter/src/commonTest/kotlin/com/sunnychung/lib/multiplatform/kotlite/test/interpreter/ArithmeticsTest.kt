package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

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
        assertEquals(3, (symbolTable.propertyValues["x"] as IntValue).value)
        assertEquals(44, (symbolTable.propertyValues["y"] as IntValue).value)
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
        assertEquals(3, (symbolTable.propertyValues["x"] as IntValue).value)
        assertEquals(44, (symbolTable.propertyValues["y"] as IntValue).value)
        assertEquals(50, (symbolTable.propertyValues["z"] as IntValue).value)
        assertEquals(1, (symbolTable.propertyValues["a"] as IntValue).value)
    }

    @Test
    fun prePlusPlus() {
        val interpreter = interpreter("""
            var x: Int = 20
            val y: Int = ++x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.propertyValues["x"] as IntValue).value)
        assertEquals(21, (symbolTable.propertyValues["y"] as IntValue).value)
    }

    @Test
    fun postPlusPlus() {
        val interpreter = interpreter("""
            var x: Int = 20
            val y: Int = x++
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.propertyValues["x"] as IntValue).value)
        assertEquals(20, (symbolTable.propertyValues["y"] as IntValue).value)
    }

    @Test
    fun preMinusMinus() {
        val interpreter = interpreter("""
            var x: Int = 20
            val y: Int = --x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(19, (symbolTable.propertyValues["x"] as IntValue).value)
        assertEquals(19, (symbolTable.propertyValues["y"] as IntValue).value)
    }

    @Test
    fun postMinusMinus() {
        val interpreter = interpreter("""
            var x: Int = 20
            val y: Int = x--
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(19, (symbolTable.propertyValues["x"] as IntValue).value)
        assertEquals(20, (symbolTable.propertyValues["y"] as IntValue).value)
    }
}