package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassMemberPropertyAccessorTest {

    @Test
    fun get1() {
        val interpreter = interpreter("""
            var a: Int = -10
            class MyCls {
                var a: Int = 1
                val b: Int
                    get() = a
            }
            val o: MyCls = MyCls()
            val x: Int = o.b
            o.a = 5
            val y: Int = o.b
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
        assertEquals(-10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun get2() {
        val interpreter = interpreter("""
            var a: Int = -10
            class MyCls {
                var a: Int = 1
                val b: Int
                    get() {
                        a += 20
                        return 10
                    }
            }
            val o: MyCls = MyCls()
            val x: Int = o.a
            val y: Int = o.b
            val z: Int = o.a
            val y2: Int = o.b
            val z2: Int = o.a
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("y2") as IntValue).value)
        assertEquals(41, (symbolTable.findPropertyByDeclaredName("z2") as IntValue).value)
        assertEquals(-10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun set1() {
        val interpreter = interpreter("""
            var a: Int = -10
            class MyCls {
                var a: Int = 1
                var b: Int
                    set(value) {
                        a += value
                    }
            }
            val o: MyCls = MyCls()
            val x: Int = o.a
            o.b = 20
            val y: Int = o.a
            o.b = 6
            val z: Int = o.a
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
        assertEquals(27, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertEquals(-10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun set2() {
        val interpreter = interpreter("""
            var a: Int = -10
            class MyCls {
                var a: Int = 1
                var b: Int
                    set(value) {
                        a += value
                        this.c = value
                    }
                var c: Int = 2
            }
            val o: MyCls = MyCls()
            val x: Int = o.a
            o.b = 20
            val y: Int = o.a
            o.b = 6
            val z: Int = o.a
            val c: Int = o.c
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
        assertEquals(27, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
        assertEquals(-10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun getAndSet() {
        val interpreter = interpreter("""
            var a: Int = -10
            class MyCls {
                var a: Int = 1
                var b: Int
                    get() {
                        return a
                    }
                    set(value) {
                        a += value
                    }
            }
            val o: MyCls = MyCls()
            val xa: Int = o.a
            val xb: Int = o.a
            o.b = 20
            val ya: Int = o.a
            val yb: Int = o.a
            o.b = 6
            val za: Int = o.a
            val zb: Int = o.a
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("xa") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("xb") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("ya") as IntValue).value)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("yb") as IntValue).value)
        assertEquals(27, (symbolTable.findPropertyByDeclaredName("za") as IntValue).value)
        assertEquals(27, (symbolTable.findPropertyByDeclaredName("zb") as IntValue).value)
        assertEquals(-10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }
}