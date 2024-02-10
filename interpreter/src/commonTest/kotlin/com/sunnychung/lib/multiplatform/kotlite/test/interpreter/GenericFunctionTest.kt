package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenericFunctionTest {

    @Test
    fun simpleGenericParameter() {
        val interpreter = interpreter("""
            fun <T> myToString(x: T): String = "${'$'}x"
            val a = myToString<Int>(10)
            val b = myToString<Double>(2.345)
            val c = myToString<Long>(20L)
            val d = myToString<String>("abc")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("10", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("2.345", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("20", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    fun genericLambdaParameter() {
        val interpreter = interpreter("""
            fun <T> myToString(value: T, delegate: (T) -> String): String = delegate(value)
            val a = myToString<Int>(10) { x: Int -> "a${'$'}x" }
            val b = myToString<Double>(2.345) { x: Double -> "b${'$'}x" }
            val c = myToString<Long>(20L) { x: Long -> "c${'$'}x" }
            val d = myToString<String>("abc") { x: String -> "d${'$'}x" }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("a10", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("b2.345", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("c20", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("dabc", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    @Ignore
    fun inferGenericLambdaParameter() {
        val interpreter = interpreter("""
            fun <T> myToString(value: T, delegate: (T) -> String): String = delegate(value)
            val a = myToString<Int>(10) { x -> "a${'$'}x" }
            val b = myToString<Double>(2.345) { x -> "b${'$'}x" }
            val c = myToString<Long>(20L) { x -> "c${'$'}x" }
            val d = myToString<String>("abc") { x -> "d${'$'}x" }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("a10", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("b2.345", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("c20", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("dabc", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    fun simpleGenericParameterAndReturn() {
        val interpreter = interpreter("""
            class A(val x: Int)
            fun <T> identity(a: T): T = a
            val a: Int = identity<Int>(123)
            val b: Double = identity<Double>(4.5)
            val c: String = identity<String>("abc")
            val d: Long = identity<Long>(678L)
            val e: Int = identity<A>(A(90)).x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(5, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        compareNumber(4.5, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(678L, (symbolTable.findPropertyByDeclaredName("d") as LongValue).value)
        assertEquals(90, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun genericFunctionCallsAnotherGenericFunction1() {
        val interpreter = interpreter("""
            fun <T> f1(a: T): T = a
            fun <T> f2(a: T): T = f1<T>(a)
            val a: Int = f2<Int>(123)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun genericFunctionCallsAnotherGenericFunction2() {
        val interpreter = interpreter("""
            fun <T1> f1(a: T1): T1 = a
            fun <T2> f2(a: T2): T2 = f1<T2>(a)
            val a: Int = f2<Int>(123)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun typeParameterHasSameNameWithType() {
        val interpreter = interpreter("""
            class T(val x: Int)
            fun <T> identity(a: T): T = a
            val a: Int = identity<Int>(123)
            val b: String = identity<String>("abc")
            val c: T = identity<T>(T(90))
            val d: Int = c.x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertTrue(symbolTable.findPropertyByDeclaredName("c") is ClassInstance)
        assertEquals(90, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun multipleGenericParameters() {
        val interpreter = interpreter("""
            fun <A, B> join(x: A, y: B): String = "${'$'}x,${'$'}y"
            val a = join<Int, Double>(10, 2.345)
            val b = join<Long, String>(67L, "abc")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("10,2.345", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("67,abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun functionOverload() {
        val interpreter = interpreter("""
            fun <A> join(x: A): String = "${'$'}x"
            fun <A, B> join(x: A, y: B): String = "${'$'}x,${'$'}y"
            fun <A, B, C> join(x: A, y: B, z: C): String = "${'$'}x,${'$'}y,${'$'}z"
            val a = join<Double>(12.345)
            val b = join<Long, String>(67L, "abc")
            val c = join<Long, Int, String>(67L, 123, "abc")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("12.345", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("67,abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("67,123,abc", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
    }

    @Test
    fun genericVararg() {
        val interpreter = interpreter("""
            var a: Int = 0
            fun <T> f(vararg args: T) {
                ++a
            }
            f(1)
            f(null)
            f(1, 2, 3)
            f("abc", "def", "ghijk")
            f(1, "abc", 4.5, true)
            f(null, 2, 3, null)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun nonAnyTypeUpperBound() {
        val interpreter = interpreter("""
            open class MyStringListBase<T> {
                var elements = ""
                fun add(element: T) {
                    elements += "${'$'}element,"
                }
            }
            
            class StringList : MyStringListBase<String>()
            class IntList : MyStringListBase<Int>()
            
            fun <D : MyStringListBase<*>, O : MyStringListBase<*>> addAll(destination: D, list: O) {
                destination.elements += list.elements
            }
            val x = StringList()
            val y = IntList()
            x.add("abc")
            x.add("def")
            y.add(123)
            y.add(45)
            x.add("bcd")
            addAll(x, y)
            val a = x.elements
            val b = y.elements
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abc,def,bcd,123,45,", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("123,45,", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }
}
