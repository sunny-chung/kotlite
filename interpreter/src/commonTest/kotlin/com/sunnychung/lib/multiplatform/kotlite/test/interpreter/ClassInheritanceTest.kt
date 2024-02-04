package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ClassInheritanceTest {

    @Test
    fun inheritProperty() {
        val interpreter = interpreter("""
            class A {
                var a = 1
            }
            class B(var b: Int) : A()
            
            val x = B(123)
            val a = x.a
            val b = x.b
            x.a += 2
            val c = x.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun inheritPropertyInConstructor() {
        val interpreter = interpreter("""
            class A(val c: Int) {
                var a = 1
            }
            class B(var b: Int) : A(b + 10)
            
            val x = B(123)
            val a = x.a
            val b = x.b
            x.a += 2
            val c = x.a
            val d = x.c
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(133, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }
}
