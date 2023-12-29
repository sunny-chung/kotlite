package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ClassInitializerTest {
    @Test
    fun primaryConstructor() {
        val interpreter = interpreter("""
            class Cls(a: Int, b: Int = 3 + a, val c: Int, val d: Int = a + b + c)
            val o: Cls = Cls(a = 10, c = 2)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        val o = symbolTable.findPropertyByDeclaredName("o") as ClassInstance
        assertEquals(2, o.memberPropertyValues.size)
        assertFails { o.findPropertyByDeclaredName("a") }
        assertFails { o.findPropertyByDeclaredName("b") }
        assertEquals(2, (o.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(25, (o.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun classMemberVariableShadowsPrimaryConstructorParameter() {
        val interpreter = interpreter("""
            class Cls(a: Int, b: Int = 3 + a, val c: Int, val d: Int = a + b + c) {
                val a: Int = a + b
                val e: Int = 5
            }
            val o: Cls = Cls(a = 10, c = 2)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        val o = symbolTable.findPropertyByDeclaredName("o") as ClassInstance
        assertEquals(4, o.memberPropertyValues.size)
        assertEquals(23, (o.findPropertyByDeclaredName("a") as IntValue).value)
        assertFails { o.findPropertyByDeclaredName("b") }
        assertEquals(2, (o.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(25, (o.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(5, (o.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun simpleInit() {
        val interpreter = interpreter("""
            class Cls {
                var a: Int = 10
                init {
                    a = 25
                }
            }
            val o: Cls = Cls()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        val o = symbolTable.findPropertyByDeclaredName("o") as ClassInstance
        assertEquals(1, o.memberPropertyValues.size)
        assertEquals(25, (o.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun multipleInit() {
        val interpreter = interpreter("""
            class Cls {
                var a: Int = 10
                init {
                    a = 25
                }
                init {
                    a = 29
                }
            }
            val o: Cls = Cls()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        val o = symbolTable.findPropertyByDeclaredName("o") as ClassInstance
        assertEquals(1, o.memberPropertyValues.size)
        assertEquals(29, (o.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun initOrder() {
        val interpreter = interpreter("""
            var i: Int = 0
            class Cls(var a: Int = ++i) {
                var b: Int = ++i
                var c: Int = ++i
                init {
                    b = ++i
                }
                var d: Int = ++i
                var e: Int = ++i
                init {
                    e = ++i
                }
                var f: Int = ++i
            }
            val o: Cls = Cls()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        val o = symbolTable.findPropertyByDeclaredName("o") as ClassInstance
        assertEquals(6, o.memberPropertyValues.size)
        assertEquals(1, (o.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(4, (o.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (o.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(5, (o.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(7, (o.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(8, (o.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun complicatedShadows() {
        val interpreter = interpreter("""
            var a: Int = 10
            class Cls(a: Int) {
                var a: Int = a + 10
                var b: Int = 0
                var c: Int = 0
                var d: Int = 0
                var e: Int = 0
                init {
                    b = a
                    val a: Int = 40
                    c = a
                }
                var f: Int = a
                var g: Int = a
                init {
                    d = a
                    val a: Int = a + 30
                    if (a > 10) {
                        val a: Int = a + 10
                        g = a
                    }
                    e = a
                }
            }
            val o: Cls = Cls(a + 10)
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        val o = symbolTable.findPropertyByDeclaredName("o") as ClassInstance
        assertEquals(7, o.memberPropertyValues.size)
        assertEquals(30, (o.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(20, (o.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(40, (o.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(20, (o.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(50, (o.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(20, (o.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(60, (o.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun multipleReadWriteInPropertyInit() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 10) {
                var b: Int = ++a
                var c: Int = (++a) + (++b)
                var d: Int = b++
                var e: Int = b
                var f: Int = a
            }
            val o: Cls = Cls()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
        val o = symbolTable.findPropertyByDeclaredName("o") as ClassInstance
        assertEquals(6, o.memberPropertyValues.size)
        assertEquals(12, (o.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(13, (o.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(24, (o.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(12, (o.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(13, (o.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(12, (o.findPropertyByDeclaredName("f") as IntValue).value)
    }
}