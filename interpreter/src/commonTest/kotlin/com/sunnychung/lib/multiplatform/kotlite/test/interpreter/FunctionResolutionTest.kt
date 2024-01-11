package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionResolutionTest {

    @Test
    fun functionsHaveHigherPriorityThanPropertiesWithinSameScope() {
        val interpreter = interpreter("""
            fun f(): Int = 2
            val f = { 3 }
            val a = f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun propertiesInThisScopeHaveHigherPriorityThanFunctionsInParentScope() {
        val interpreter = interpreter("""
            fun f(): Int = 2
            fun g(f: () -> Int): Int {
                return f()
            }
            val a = g { 3 }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun classFunctionsAlwaysHaveHigherPriorityThanExtensionFunctions1() {
        val interpreter = interpreter("""
            class A {
                fun f(): Int = 2
            }
            fun A.f(): Int = 3
            val a = A().f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun classFunctionsAlwaysHaveHigherPriorityThanExtensionFunctions2() {
        val interpreter = interpreter("""
            class A {
                fun f(): Int = 2
            }
            fun g(): Int {
                fun A.f(): Int = 3
                return A().f()
            }
            val a = g()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun classFunctionsHaveHigherPriorityThanClassProperty1() {
        val interpreter = interpreter("""
            class A {
                val f: () -> Int = { 3 }
                fun f(): Int = 2
                
                fun g(): Int = f()
            }
            val a = A().f()
            val b = A().g()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun classFunctionsHaveHigherPriorityThanClassProperty2() {
        val interpreter = interpreter("""
            class A {
                fun f(): Int = 2
                val f: () -> Int = { 3 }
                
                fun g(): Int = f()
            }
            val a = A().f()
            val b = A().g()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
