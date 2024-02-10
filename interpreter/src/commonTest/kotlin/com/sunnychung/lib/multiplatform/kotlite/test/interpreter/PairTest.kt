package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class PairTest {
    @Test
    fun simplePairCreatedUsingToInfix() {
        val interpreter = interpreter("""
            val p: Pair<String, String> = "abc" to "def"
            val a: String = p.first
            val b: String = p.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun pairOfDifferentTypes() {
        val interpreter = interpreter("""
            val p: Pair<String, Int> = "abc" to (123 + 4)
            val a: String = p.first
            val b: Int = p.second
            
            val q = b to 45.6
            val c = q.first
            val d = q.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(127, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(127, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        compareNumber(45.6, symbolTable.findPropertyByDeclaredName("d") as DoubleValue)
    }

    @Test
    fun pairOfNestedGenericClasses() {
        val interpreter = interpreter("""
            class Value<T>(val value: T)
            class A(val a: Int, val b: String)
            class B(val x: Value<Double>)
            val p = Value(Value(A(123, "abc"))) to Value(B(Value(45.6)))
            val a: Int = p.first.value.value.a
            val b: String = p.first.value.value.b
            val c = p.second.value.x.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        compareNumber(45.6, symbolTable.findPropertyByDeclaredName("c") as DoubleValue)
    }

    @Test
    fun nestedPairs() {
        val interpreter = interpreter("""
            val x = 67 to "abc"
            val p = 12 to ( (345 to x) to 89 )
            val a = p.first
            val b = p.second.first.first
            val c = p.second.first.second.first
            val d = p.second.first.second.second
            val e = p.second.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(67, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
        assertEquals(89, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }
}
