package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ClassInheritanceTest {

    @Test
    fun inheritProperty() {
        val interpreter = interpreter("""
            open class A {
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
            open class A(val c: Int) {
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

    @Test
    fun passArgumentToSuperConstructor() {
        val interpreter = interpreter("""
            open class A(val c: Int) {
                var a = 1
            }
            class B(b: Int) : A(b + 10)
            
            val x = B(123)
            val a = x.a
            x.a += 2
            val c = x.a
            val d = x.c
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(133, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun inheritFunction() {
        val interpreter = interpreter("""
            open class A {
                var a = 1
                fun hello() = a
            }
            class B(var b: Int) : A()
            
            val x = B(123)
            val a = x.a
            val b = x.b
            x.a += 2
            val c = x.hello()
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
    fun inheritExtensionFunction() {
        val interpreter = interpreter("""
            open class A {
                fun Int.double() = this * 2
            }
            class B(val b: Int) : A() {
                fun doubleB() = b.double()
            }
            
            val x = B(123)
            val a = x.doubleB()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(246, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun overrideMemberFunction() {
        val interpreter = interpreter("""
            var a = 1
            open class A {
                open fun f() {
                    a += 5
                }
            }
            class B : A() {
                override fun f() {
                    a += 7
                }
            }
            
            val x = B()
            x.f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun memberFunctionsAreNotOverriddenIfSignaturesAreDifferent() {
        val interpreter = interpreter("""
            var a = 1
            open class A {
                fun f(x: Int) {
                    a += 5
                }
            }
            class B : A() {
                fun f(x: String) {
                    a += 7
                }
            }
            
            val x = B()
            x.f(123)
            val b = a
            x.f("abc")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(13, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
