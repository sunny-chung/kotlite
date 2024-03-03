package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class JumpTest {

    @Test
    fun returnToLambdaProperty() {
        val interpreter = interpreter("""
            val f = abc@ { x: Int ->
                if (x < 10) {
                    return@abc 1
                }
                10
            }
            val a: Int = f(1)
            val b: Int = f(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun returnToLambdaGeneratedByFunction() {
        val interpreter = interpreter("""
            fun f() = abc@ { x: Int ->
                if (x < 10) {
                    return@abc 1
                }
                10
            }
            val a: Int = f()(1)
            val b: Int = f()(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun returnToLambdaPropertyWithoutReturnValue() {
        val interpreter = interpreter("""
            var y = 0
            val f = abc@ { x: Int ->
                if (x < 10) {
                    return@abc
                }
                y += 1
            }
            f(1)
            val a: Int = y
            f(20)
            val b: Int = y
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun returnToLambdaGeneratedByFunctionWithoutReturnValue() {
        val interpreter = interpreter("""
            var y = 0
            fun f() = abc@ { x: Int ->
                if (x < 10) {
                    return@abc
                }
                y += 1
            }
            f()(1)
            val a: Int = y
            f()(20)
            val b: Int = y
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
