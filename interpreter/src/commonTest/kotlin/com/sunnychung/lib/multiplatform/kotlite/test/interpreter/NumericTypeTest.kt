package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class NumericTypeTest {

    @Test
    fun positiveIntegerZeroAndNegativeInteger() {
        val interpreter = interpreter("""
            val a: Int = 0
            val b: Int = 15
            val c: Int = -12
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(-12, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun positiveIntegerZeroAndNegativeIntegerWithDot() {
        val interpreter = interpreter("""
            fun <T> T.f(): T = this
            val a: Int = 0.f()
            val b: Int = 15.f()
            val c: Int = -12.f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(-12, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }
}
