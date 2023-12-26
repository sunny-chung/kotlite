package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class BooleanTest {

    @Test
    fun and() {
        val interpreter = interpreter("""
            val x: Int = 10
            val a: Boolean = x > 5 && x < 15
            val b: Boolean = x > 5 && x > 15
            val c: Boolean = x < 5 && x < 15
            val d: Boolean = x < 5 && x > 15
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }

    @Test
    fun or() {
        val interpreter = interpreter("""
            val x: Int = 10
            val a: Boolean = x > 5 || x < 15
            val b: Boolean = x > 5 || x > 15
            val c: Boolean = x < 5 || x < 15
            val d: Boolean = x < 5 || x > 15
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }

    @Test
    fun orShortCircuit() {
        val interpreter = interpreter("""
            var x: Int = 0
            var y: Int = 0
            
            fun f(): Int {
                ++x
                return 10
            }
            
            fun g(): Int {
                ++y
                return 10
            }
            
            val a: Boolean = f() > 5 || g() < 15
            val ax: Int = x; val ay: Int = y; x = 0; y = 0;
            
            val b: Boolean = f() > 5 || g() > 15
            val bx: Int = x; val by: Int = y; x = 0; y = 0;
            
            val c: Boolean = f() < 5 || g() < 15
            val cx: Int = x; val cy: Int = y; x = 0; y = 0;
            
            val d: Boolean = f() < 5 || g() > 15
            val dx: Int = x; val dy: Int = y; x = 0; y = 0;
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(14, symbolTable.propertyValues.size)

        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("ax") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("ay") as IntValue).value)

        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("bx") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("by") as IntValue).value)

        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("cx") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("cy") as IntValue).value)

        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("dx") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("dy") as IntValue).value)
    }

    @Test
    fun andShortCircuit() {
        val interpreter = interpreter("""
            var x: Int = 0
            var y: Int = 0
            
            fun f(): Int {
                ++x
                return 10
            }
            
            fun g(): Int {
                ++y
                return 10
            }
            
            val a: Boolean = f() > 5 && g() < 15
            val ax: Int = x; val ay: Int = y; x = 0; y = 0;
            
            val b: Boolean = f() > 5 && g() > 15
            val bx: Int = x; val by: Int = y; x = 0; y = 0;
            
            val c: Boolean = f() < 5 && g() < 15
            val cx: Int = x; val cy: Int = y; x = 0; y = 0;
            
            val d: Boolean = f() < 5 && g() > 15
            val dx: Int = x; val dy: Int = y; x = 0; y = 0;
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(14, symbolTable.propertyValues.size)

        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("ax") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("ay") as IntValue).value)

        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("bx") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("by") as IntValue).value)

        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("cx") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("cy") as IntValue).value)

        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("dx") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("dy") as IntValue).value)
    }

    @Test
    fun not() {
        val interpreter = interpreter("""
            val x: Int = 10
            val y: Boolean = false
            val a: Boolean = !y
            val b: Boolean = !true
            val c: Boolean = !(x < 5)
            val d: Boolean = !!(!!!(x > 5))
            val e: Boolean = !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!true
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
    }

    @Test
    fun mixedBooleanArithmetics() {
        val interpreter = interpreter("""
            val x: Int = 10
            val y: Int = 20
            val a: Boolean = ((!(x > 5 && y < 20)) || (y > 10)) && !(y < 0 || false) && true
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
    }
}
