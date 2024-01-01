package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NumberValue
import com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis.assertSemanticFail
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionFunctionTest {

    @Test
    fun intExtensionWithoutThis() {
        val interpreter = interpreter("""
            fun Int.f(): Int {
                return 10
            }
            val x: Int = 2
            val y: Int = x.f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun intExtensionWithThis() {
        val interpreter = interpreter("""
            fun Int.triple(): Int {
                return 3 * this
            }
            val x: Int = 2
            val y: Int = x.triple()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun intExtensionOfValue() {
        val interpreter = interpreter("""
            fun Int.triple(): Int {
                return 3 * this
            }
            val x: Int = 2.triple()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun doubleExtensionWithThis() {
        val interpreter = interpreter("""
            fun Double.triple(): Double {
                return 3 * this
            }
            val x: Double = 2.1
            val y: Double = x.triple()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        compareNumber(6.3, symbolTable.findPropertyByDeclaredName("y") as NumberValue<*>)
    }

    @Test
    fun doubleExtensionOfValue() {
        val interpreter = interpreter("""
            fun Double.triple(): Double {
                return 3 * this
            }
            val x: Double = 2.1.triple()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        compareNumber(6.3, symbolTable.findPropertyByDeclaredName("x") as NumberValue<*>)
    }

    @Test
    fun classExtension() {
        val interpreter = interpreter("""
            class Cls(var a: Int)
            fun Cls.f() {
                ++a
            }
            val o: Cls = Cls(20)
            o.f()
            o.f()
            val x: Int = o.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun classExtensionWithShadowedVariable() {
        val interpreter = interpreter("""
            class Cls(var a: Int)
            fun Cls.f(): Int {
                val a: Int = a++
                return a + 100
            }
            val o: Cls = Cls(20)
            o.f()
            val x: Int = o.f()
            val y: Int = o.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(121, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun classExtensionOnNestedPath() {
        val interpreter = interpreter("""
            class B(var a: Int)
            class A(val b: B)
            val o: A = A(B(20))
            fun B.f() {
                ++a
            }
            o.b.f()
            o.b.f()
            val x: Int = o.b.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun extensionInsideClassAccessingProperties() {
        val interpreter = interpreter("""
            class B(var a: Int)
            class A(val b: B, var c: Int) {
                fun B.f() {
                    a += c++
                }
                
                fun a() {
                    b.f()
                }
            }
            val o: A = A(B(30), 9)
            o.a()
            o.a()
            val x: Int = o.b.a
            val y: Int = o.c
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(49, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun extensionsInsideClassAreNotCallableOutside() {
        assertSemanticFail("""
            class B(var a: Int)
            class A(val b: B, var c: Int) {
                fun B.f() {
                    a += c++
                }
                
                fun a() {
                    b.f()
                }
            }
            val b: B = B(30)
            val o: A = A(b, 9)
            o.a()
            o.a()
            val x: Int = o.b.a
            val y: Int = o.c
            b.f()
        """.trimIndent())
    }

    @Test
    fun extensionInsideClassAccessingFunctions() {
        val interpreter = interpreter("""
            class B(var a: Int) {
                fun inc(x: Int) {
                    a += x
                }
            }
            class A(val b: B, var c: Int) {
                fun addC(): Int {
                    return c++
                }
                
                fun B.f() {
                    inc(addC())
                }
                
                fun a() {
                    b.f()
                }
            }
            val o: A = A(B(30), 9)
            o.a()
            o.a()
            val x: Int = o.b.a
            val y: Int = o.c
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(49, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }
}
