package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.CharValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class CharTest {

    @Test
    fun compareCharEquality() {
        val interpreter = interpreter("""
            val x: Char = 'k'
            val y = 'k'
            val a = x == y
            val b = x != y
            val c = x == 'k'
            val d = x != 'k'
            val e = x == 'K'
            val f = x != 'K'
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
    }

    @Test
    fun compareChar() {
        val interpreter = interpreter("""
            val x = 'k'
            val y = 'w'
            val a = x < y
            val b = x > y
            val c = x <= 'k'
            val d = x >= 'k'
            val e = x <= 'K'
            val f = x >= 'K'
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
    }

    @Test
    fun compareOtherType() {
        val interpreter = interpreter("""
            val x = 'k'
            val a = x == "K"
            val b = x != "K"
            val c = x == 123
            val d = x != 123
            val e = x == 123.45
            val f = x != 123.45
            val g = x == true
            val h = x != true
            val i = x == null
            val j = x != null
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(11, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("i") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("j") as BooleanValue).value)
    }

    @Test
    fun charAddInt() {
        val interpreter = interpreter("""
            val x = 'k'
            val a = x + 12
            val b = 'A' + 6
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals('w', (symbolTable.findPropertyByDeclaredName("a") as CharValue).value)
        assertEquals('G', (symbolTable.findPropertyByDeclaredName("b") as CharValue).value)
    }

    @Test
    fun charSubtractChar() {
        val interpreter = interpreter("""
            val a: Int = 'k' - 's'
            val b = 'A' - '0'
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(-8, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(17, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun addString() {
        val interpreter = interpreter("""
            val a = "a" + 'b'
            val b = 'A' + "B"
            var c = b
            c += 'c'
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("ab", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("AB", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("ABc", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
    }
}
