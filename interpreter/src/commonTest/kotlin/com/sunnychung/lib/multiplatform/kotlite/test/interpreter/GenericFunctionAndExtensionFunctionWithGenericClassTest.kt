package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class GenericFunctionAndExtensionFunctionWithGenericClassTest {

    @Test
    fun unrelatedTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
                fun <T> identity(value: T): T = value
            }
            val o = MyVal1<String>("def")
            val a: String = o.getValue()
            val b: Int = o.identity<Int>(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun useBothClassTypeParameterAndFunctionTypeParameter() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
                fun <T> toPair(value: T): MyPair<A, T> = MyPair<A, T>(this.value, value)
            }
            val o = MyVal1<String>("def")
            val p = o.toPair<Int>(20)
            val a: String = p.first
            val b: Int = p.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseUnrelatedTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun <T, X> MyVal1<X>.identity(value: T): T = value
            val o = MyVal1<String>("def")
            val a: String = o.getValue()
            val b: Int = o.identity<Int, String>(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseBothClassTypeParameterAndFunctionTypeParameter() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun <T, X> MyVal1<X>.toPair(value: T): MyPair<X, T> = MyPair<X, T>(this.value, value)
            val o = MyVal1<String>("def")
            val p = o.toPair<Int, String>(20)
            val a: String = p.first
            val b: Int = p.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseStarAsTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun MyVal1<*>.individualFunc(): Int = 20
            val o = MyVal1<String>("def")
            val a: String = o.getValue()
            val b: Int = o.individualFunc()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseStarAndUnrelatedTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun <T> MyVal1<*>.identity(value: T): T = value
            val o = MyVal1<String>("def")
            val a: String = o.getValue()
            val b: Int = o.identity<Int>(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun multipleExtensionFunctionsWithSameNameButDifferentTypeArguments() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun MyVal1<String>.func(): String = "abc" + value
            fun MyVal1<Int>.func(): Int = value * 2
            val a: String = MyVal1<String>("def").func()
            val b: Int = MyVal1<Int>(10).func()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abcdef", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
