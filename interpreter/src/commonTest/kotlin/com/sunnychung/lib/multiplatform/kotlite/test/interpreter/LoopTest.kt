package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.CollectionInterface
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.IterableInterface
import com.sunnychung.lib.multiplatform.kotlite.model.ListValue
import kotlin.test.Test
import kotlin.test.assertEquals

class LoopTest {

    @Test
    fun simpleWhileLoop() {
        val interpreter = interpreter("""
            var x: Int = 1
            while (x < 100) {
                x += 1
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(100, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun simpleWhileLoopStatement() {
        val interpreter = interpreter("""
            var x: Int = 1
            while (x < 100) x += 1
            x += 20
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(120, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun whileLoopWithBreak() {
        val interpreter = interpreter("""
            var x: Int = 1
            while (x < 100) {
                x += 1
                if (x > 50) break
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(51, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun whileLoopWithContinue() {
        val interpreter = interpreter("""
            var x: Int = 1
            var sum: Int = 0
            while (
                x < 10
            ) {
                x += 1
                if (x % 2 == 1) {
                    continue
                }
                sum += x
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(2 + 4 + 6 + 8 + 10, (symbolTable.findPropertyByDeclaredName("sum") as IntValue).value)
    }

    @Test
    fun whileLoopWithoutBody1() {
        val interpreter = interpreter("""
            var x: Int = 0
            while (++x < 1000000);
            x += 10
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(1000010, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun whileLoopWithoutBody2() {
        val interpreter = interpreter("""
            var x: Int = 0
            while (x++ < 1000000);
            x += 10
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(1000011, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun forLoop() {
        val interpreter = interpreter("""
            var x: Int = 0
            var sum: Int = 0
            var numCreate = 0
            fun createList(vararg values: Int): List<Int> {
                ++numCreate
                return values
            }
            for (i in createList(1, 5, 2)) {
                ++x
                sum += i
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("sum") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("numCreate") as IntValue).value)
    }
}
