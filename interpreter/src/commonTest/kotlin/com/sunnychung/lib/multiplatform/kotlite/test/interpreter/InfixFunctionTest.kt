package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class InfixFunctionTest {

    @Test
    fun simpleInfixFunction() {
        val interpreter = interpreter("""
            class IntBox(val value: Int) {
                infix fun add(other: IntBox): IntBox {
                    return IntBox(value + other.value)
                }
            }
            val x: IntBox = IntBox(7) add IntBox(13)
            val y: IntBox = x add IntBox(-6)
            val a = x.value
            val b = y.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun chainInfixFunction() {
        val interpreter = interpreter("""
            class IntBox(val value: Int) {
                infix fun add(other: IntBox): IntBox {
                    return IntBox(value + other.value)
                }
            }
            val x: IntBox = IntBox(7) add IntBox(13) add IntBox(-1) add IntBox(10)
            val a = x.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun infixExtensionFunction() {
        val interpreter = interpreter("""
            infix fun String.concat(other: String) = this + other
            val a: String = "abc" concat "de" concat "fghijk"
            val b = "a" concat "bcd"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abcdefghijk", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("abcd", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun inheritedInfixFunction() {
        val interpreter = interpreter("""
            open class IntBox(val value: Int) {
                infix fun add(other: IntBox): IntBox {
                    return IntBox(value + other.value)
                }
            }
            abstract class A(value: Int) : IntBox(value)
            open class B(value: Int) : A(value)
            class C(value: Int) : B(value)
            val x: IntBox = C(7) add B(13) add C(-1) add C(10)
            val a = x.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun overrideInfixFunction() {
        val interpreter = interpreter("""
            open class IntBox(val value: Int) {
                infix open fun add(other: IntBox): IntBox {
                    return IntBox(value + other.value)
                }
            }
            open class A(value: Int) : IntBox(value)
            open class B(value: Int) : A(value) {
                override infix fun add(other: IntBox): IntBox {
                    return B(value + other.value * 3)
                }
            }
            open class C(value: Int) : B(value)
            class D(value: Int) : C(value)
            val a = (C(7) add B(13) add D(-1) add A(10)).value
            val b = (A(7) add B(13) add C(-1) add A(10)).value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(73, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionInfixFunctionOfBaseClass() {
        val interpreter = interpreter("""
            open class IntBox(val value: Int)
            open class A(value: Int) : IntBox(value)
            open class B(value: Int) : A(value)
            open class C(value: Int) : B(value)
            class D(value: Int) : C(value)
            infix fun IntBox.add(other: IntBox): IntBox {
                return IntBox(value + other.value)
            }
            val a: IntBox = C(7) add B(13) add D(-1) add A(10)
            val b = a.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionInfixFunctionOfBaseClassAndSubClass() {
        val interpreter = interpreter("""
            open class IntBox(val value: Int)
            open class A(value: Int) : IntBox(value)
            open class B(value: Int) : A(value)
            open class C(value: Int) : B(value)
            class D(value: Int) : C(value)
            infix fun IntBox.add(other: IntBox): IntBox {
                return IntBox(value + other.value)
            }
            infix fun B.add(other: IntBox): B {
                return B(value + other.value * 3)
            }
            val a = (C(7) add B(13) add D(-1) add A(10)).value
            val b = ((C(7) as IntBox) add B(13) add D(-1) add A(10)).value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(73, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun genericInfixExtensionFunction() {
        val interpreter = interpreter("""
            open class Box<T>(val value: Int)
            open class A<T>(value: Int) : Box<T>(value)
            open class B<X>(value: Int) : A<X>(value)
            open class C<T>(value: Int) : B<T>(value)
            class D<T>(value: Int) : C<T>(value)
            infix fun <T> Box<T>.add(other: Box<T>): Box<T> {
                return Box<T>(value + other.value)
            }
            val a: Box<Int> = C<Int>(7) add B<Int>(13) add D<Int>(-1) add A<Int>(10)
            val b = a.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
