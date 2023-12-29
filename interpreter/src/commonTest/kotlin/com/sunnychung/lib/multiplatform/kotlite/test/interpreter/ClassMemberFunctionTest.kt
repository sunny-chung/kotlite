package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassMemberFunctionTest {
    @Test
    fun simpleFunctions() {
        val interpreter = interpreter("""
            class MyCls {
                var a: Int = 1
                var b: Int = 2
                
                fun addA() {
                    a += 10
                }
                
                fun getA(): Int {
                    return this.a
                }
                
                fun addB() {
                    this.b += 11
                }
                
                fun getB(): Int {
                    return b
                }
            }
            val o1: MyCls = MyCls()
            val o2: MyCls = MyCls()
            o1.addA()
            o2.addA()
            o2.addB()
            o1.addB()
            o2.addA()
            o2.addA()
            o2.addB()
            val a1: Int = o1.getA()
            val b1: Int = o1.getB()
            val a2: Int = o2.getA()
            val b2: Int = o2.getB()
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o1") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("o2") is ClassInstance)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals(13, (symbolTable.findPropertyByDeclaredName("b1") as IntValue).value)
        assertEquals(31, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("b2") as IntValue).value)
    }

    @Test
    fun functionsWithShadowVariables() {
        val interpreter = interpreter("""
            class MyCls {
                var a: Int = 1
                var b: Int = 2
                
                fun addA() {
                    a += 10
                    val a: Int = 100
                }
                
                fun getA(): Int {
                    return this.a
                }
                
                fun addB() {
                    val b: Int = 200
                    b += 11
                    this.b += 50
                }
                
                fun getB(): Int {
                    return b
                }
            }
            val o1: MyCls = MyCls()
            val o2: MyCls = MyCls()
            o1.addA()
            o2.addA()
            o2.addB()
            o1.addB()
            o2.addA()
            o2.addA()
            o2.addB()
            val a1: Int = o1.getA()
            val b1: Int = o1.getB()
            val a2: Int = o2.getA()
            val b2: Int = o2.getB()
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o1") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("o2") is ClassInstance)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals(52, (symbolTable.findPropertyByDeclaredName("b1") as IntValue).value)
        assertEquals(31, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals(102, (symbolTable.findPropertyByDeclaredName("b2") as IntValue).value)
    }

    @Test
    fun functionInterCall() {
        val interpreter = interpreter("""
            class MyCls {
                var a: Int = 1
                var other: MyCls? = null
                
                fun funcA(): Int {
                    return ++a + other.funcB()
                }
                
                fun funcB(): Int {
                    return other.a++
                }
            }
            val o1: MyCls = MyCls()
            val o2: MyCls = MyCls()
            o1.other = o2
            o2.other = o1
            val a: Int = o1.funcA()
            val b: Int = o1.a
        """.trimIndent(), isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
//        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("o1") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("o2") is ClassInstance)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
