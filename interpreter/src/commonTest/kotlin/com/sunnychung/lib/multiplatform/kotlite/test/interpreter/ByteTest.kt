package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ByteValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteTest {
    @Test
    fun assignIntLiteralToByte() {
        val interpreter = interpreter("""
            val a: Byte = 65
            val b: Byte = 127
            val c: Byte = -128
            val d: Byte = 0
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(65, (symbolTable.findPropertyByDeclaredName("a") as ByteValue).value)
        assertEquals(127, (symbolTable.findPropertyByDeclaredName("b") as ByteValue).value)
        assertEquals(-128, (symbolTable.findPropertyByDeclaredName("c") as ByteValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("d") as ByteValue).value)
    }

    @Test
    fun compareByteWithByte() {
        listOf(69, 65, 101).forEach { x ->
            listOf(69, 65, 101).forEach { y ->
                val interpreter = interpreter("""
                    val x: Byte = $x
                    val y: Byte = $y
                    val a = x > y
                    val b = x < y
                    val c = x >= y
                    val d = x <= y
                    val e = x == y
                    val f = x != y
                """.trimIndent())
                interpreter.eval()
                val symbolTable = interpreter.callStack.currentSymbolTable()
                println(symbolTable.propertyValues)
                assertEquals(8, symbolTable.propertyValues.size)
                assertEquals(x > y, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
                assertEquals(x < y, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
                assertEquals(x >= y, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
                assertEquals(x <= y, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
                assertEquals(x == y, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
                assertEquals(x != y, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
            }
        }
    }

    @Test
    fun compareByteWithInt() {
        val otherType = "Int"
        listOf(69, 65, 101).forEach { x ->
            listOf(69, 65, 101).forEach { y ->
                listOf(false, true).forEach { isByteFirst ->
                    val interpreter = interpreter("""
                        val x: ${if (isByteFirst) "Byte" else otherType} = $x
                        val y: ${if (!isByteFirst) "Byte" else otherType} = $y
                        val a = x > y
                        val b = x < y
                        val c = x >= y
                        val d = x <= y
                    """.trimIndent())
                    interpreter.eval()
                    val symbolTable = interpreter.callStack.currentSymbolTable()
                    println(symbolTable.propertyValues)
                    assertEquals(6, symbolTable.propertyValues.size)
                    assertEquals(x > y, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
                    assertEquals(x < y, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
                    assertEquals(x >= y, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
                    assertEquals(x <= y, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
                }
            }
        }
    }

    @Test
    fun compareByteWithLong() {
        val otherType = "Long"
        listOf(69, 65, 101).forEach { x ->
            listOf(69, 65, 101).forEach { y ->
                listOf(false, true).forEach { isByteFirst ->
                    val interpreter = interpreter("""
                        val x: ${if (isByteFirst) "Byte" else otherType} = $x${if (!isByteFirst) "L" else ""}
                        val y: ${if (!isByteFirst) "Byte" else otherType} = $y${if (isByteFirst) "L" else ""}
                        val a = x > y
                        val b = x < y
                        val c = x >= y
                        val d = x <= y
                    """.trimIndent())
                    interpreter.eval()
                    val symbolTable = interpreter.callStack.currentSymbolTable()
                    println(symbolTable.propertyValues)
                    assertEquals(6, symbolTable.propertyValues.size)
                    assertEquals(x > y, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
                    assertEquals(x < y, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
                    assertEquals(x >= y, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
                    assertEquals(x <= y, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
                }
            }
        }
    }
}
