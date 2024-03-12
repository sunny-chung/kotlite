package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class VariableScopeTest {

    @Test
    fun sameVariableNameForGlobalScopeAndFunctionValueParameter() {
        val interpreter = interpreter("""
            val x = 11
            fun f(x: Int) = x * 3
            val a: Int = f(7)
            val b: Int = f(x)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(33, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun sameVariableNameForGlobalScopeAndClassConstructorValueParameter() {
        val interpreter = interpreter("""
            val x = 11
            class A(x: Int) {
                val y = x
                fun f() = y * 3
            }
            val a: Int = A(7).f()
            val b: Int = A(x).f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(33, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun sameVariableNameForGlobalScopeAndClassConstructorProperty() {
        val interpreter = interpreter("""
            val x = 11
            class A(val x: Int) {
                fun f() = x * 3
            }
            val a: Int = A(7).f()
            val b: Int = A(x).f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(33, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
