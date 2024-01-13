package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassMemberPropertyTest {

    @Test
    fun propertyRead() {
        val interpreter = interpreter("""
            class MyCls {
                var a: Int = 1
                var b: Int = 2
            }
            val o: MyCls = MyCls()
            val x: Int = o.a
            val y: Int = o.b
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun propertyWrite() {
        val interpreter = interpreter("""
            class MyCls {
                var a: Int = 1
                var b: Int = 2
            }
            val o: MyCls = MyCls()
            o.a = 3
            val x: Int = o.a
            val y: Int = o.b
            o.b = 4
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun multipleInstancesWrite() {
        val interpreter = interpreter("""
            class MyCls {
                var x: Int = 1
            }
            val a: MyCls = MyCls()
            val b: MyCls = MyCls()
            val c: MyCls = MyCls()
            a.x = 6
            c.x = 7
            a.x = 4
            val ax: Int = a.x
            val bx: Int = b.x
            val cx: Int = c.x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("b") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("c") is ClassInstance)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ax") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("bx") as IntValue).value)
        assertEquals(7, (symbolTable.findPropertyByDeclaredName("cx") as IntValue).value)
    }

    @Test
    fun nestedInstances() {
        val interpreter = interpreter("""
            class MyCls {
                var x: Int = 1
                var y: MyCls? = null
            }
            val a: MyCls = MyCls()
            val b: MyCls = MyCls()
            a.y = MyCls()
            a.x = 2
            a.y!!.y = MyCls()
            a.y!!.y!!.y = MyCls()
            a.y!!.y!!.y!!.y = MyCls()
            a.y!!.y!!.y!!.x = 4
            b.y = MyCls()
            b.y!!.x = 5
            val a1: Int = a.x
            val a2: Int = a.y!!.x
            val a3: Int = a.y!!.y!!.x
            val a4: Int = a.y!!.y!!.y!!.x
            val a5: Int = a.y!!.y!!.y!!.y!!.x
            val b1: Int = b.x
            val b2: Int = b.y!!.x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(9, symbolTable.propertyValues.size)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a3") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("a4") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a5") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b1") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b2") as IntValue).value)
    }
}
