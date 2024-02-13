package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import kotlin.test.Test
import kotlin.test.assertEquals

class IsOperatorTest {

    @Test
    fun primitiveTypes() {
        val interpreter = interpreter("""
            val a: Boolean = 123 is Int
            val b: Boolean = "abc" is String
            val c = 45.6 is Double
            val d = 'd' is Char
            val e = 78L is Long
            val f = false is Boolean
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
    }

    @Test
    fun specialTypes() {
        val interpreter = interpreter("""
//            val g = Unit is Unit
            val h = null is Nothing
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
//        assertEquals(true, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
    }

    @Test
    fun functionTypes() {
        val interpreter = interpreter("""
//            val i = {x: Int -> "${'$'}x"} is Function<*>
//            val i1 = {x: Int -> "${'$'}x"} is Function<String>
//            val i2 = {x: Int -> "${'$'}x"} is Function<Int>
            val i3 = {x: Int -> "${'$'}x"} is (Int) -> String
            val i4 = {x: Int -> "${'$'}x"} is (Int) -> Int
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
//        assertEquals(true, (symbolTable.findPropertyByDeclaredName("i") as BooleanValue).value)
//        assertEquals(true, (symbolTable.findPropertyByDeclaredName("i1") as BooleanValue).value)
//        assertEquals(false, (symbolTable.findPropertyByDeclaredName("i2") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("i3") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("i4") as BooleanValue).value)
    }

    @Test
    fun nonNullValueAgainstNullableType() {
        val interpreter = interpreter("""
            val a: Boolean = 123 is Int?
            val b: Boolean = "abc" is String?
            val c = 45.6 is Double?
            val d = 'd' is Char?
            val e = 78L is Long?
            val f = false is Boolean?
            // val g = Unit is Unit?
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
//        assertEquals(true, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
    }

    @Test
    fun nullValueAgainstNullableType() {
        val interpreter = interpreter("""
            val a: Boolean = null is Int?
            val b: Boolean = null is String?
            val c = null is Double?
            val d = null is Char?
            val e = null is Long?
            val f = null is Boolean?
            val g = null is Unit?
            val h = null is Nothing?
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
    }

    @Test
    fun wrongValueType() {
        val interpreter = interpreter("""
            val a: Boolean = 123.45 is Int
            val b: Boolean = true is String
            val c = "abc" is Double
            val d = 456 is Char
            val e = false is Long // Unit
            val f = 'x' is Boolean
            val g = null is Unit
            val h = 67L is Nothing
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
    }

    @Test
    fun wrongNullableValueType() {
        val interpreter = interpreter("""
            val a: Boolean = 123.45 is Int?
            val b: Boolean = true is String?
            val c = "abc" is Double?
            val d = 456 is Char?
            val e = false is Long? // Unit
            val f = 'x' is Boolean?
            val g = 89 is Unit?
            val h = 67L is Nothing?
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
    }

    @Test
    fun classType() {
        val interpreter = interpreter("""
            class A
            class B
            val j: Boolean = A() is A
            val k = B() is B
            val j1 = A() is A?
            val j2: Boolean = A() is B
            val j3 = A() is B?
            val j4 = A() is Int
            val k1 = B() is B?
            val k2 = B() is A
            val k3 = B() is A?
            val k4 = B() is Int
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(10, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("j") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("j1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("j2") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("j3") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("j4") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("k") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("k1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("k2") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("k3") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("k4") as BooleanValue).value)
    }

    @Test
    fun anyType() {
        val interpreter = interpreter("""
            class A
            class B
            val a: Boolean = 123 is Any
            val b: Boolean = "abc" is Any
            val c = 45.6 is Any
            val d = 'd' is Any
            val e = 78L is Any
            val f = false is Any
//            val g = Unit is Any
            val h = null is Any
            val i = {x: Int -> "${'$'}x"} is Any
            val j = A() is Any
            val k = B() is Any
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(10, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
//        assertEquals(true, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("i") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("j") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("k") as BooleanValue).value)
    }

    @Test
    fun nullableAnyType() {
        val interpreter = interpreter("""
            class A
            class B
            val a: Boolean = 123 is Any?
            val b: Boolean = "abc" is Any?
            val c = 45.6 is Any?
            val d = 'd' is Any?
            val e = 78L is Any?
            val f = false is Any?
//            val g = Unit is Any?
            val h = null is Any?
            val i = {x: Int -> "${'$'}x"} is Any?
            val j = A() is Any?
            val k = B() is Any?
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(10, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
//        assertEquals(true, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("i") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("j") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("k") as BooleanValue).value)
    }

    @Test
    fun genericType() {
        val interpreter = interpreter("""
            open class A
            class B : A()
            class G<T>
            val a = G<B>() is G<*>
            val b = G<B>() is G<B>
            val c = G<B>() is G<A>
            val d = G<B>() is B
            val e = G<B>() is A
            val f = G<B>() is Any
            val g = G<B>() is Any?
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
    }

    @Test
    fun nestedGenericType() {
        val interpreter = interpreter("""
            open class A
            class B : A()
            class G<T>
            val a = G<G<B>>() is G<*>
            val b = G<G<B>>() is G<G<B>>
            val c = G<G<B>>() is G<B>
            val d = G<G<B>>() is G<G<A>>
            val e = G<G<B>>() is G<A>
            val f = G<G<B>>() is Any
            val g = G<G<B>>() is Any?
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
    }

    @Test
    fun negateIsOperator() {
        val interpreter = interpreter("""
            class A
            class B
            open class C
            class D : C()
            class G<T>
            val a: Boolean = 123 !is Int
            val b: Boolean = "abc" !is String
            val c = 45.6 !is Double
            val d = 'd' !is Char
            val e = 78L !is Long
            val f = false !is Boolean
//            val g = Unit !is Unit
            val h = null !is Nothing?
            val i = {x: Int -> "${'$'}x"} !is (Int) -> String
            val j = A() !is A
            val k = B() !is B
            val l = G<D>() !is G<*>
            val m = G<G<D>>() !is G<*>
            
            val a2: Boolean = 123 !is String
            val e2 = true !is Long
            val h2 = null !is Nothing
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(15, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
//        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("i") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("j") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("k") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("l") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("m") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a2") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e2") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("h2") as BooleanValue).value)
    }
}
