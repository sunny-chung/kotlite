package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class StringTest {
    @Test
    fun simpleStringLiteral() {
        val interpreter = interpreter("""
            val s: String = "abcdef"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("abcdef", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun simpleStringUnicodeLiteral() {
        val interpreter = interpreter("""
            val s: String = "\u6211\u611b\u97F3\u6a02\uFF01\ud83c\udded\uD83C\uDDF0"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("我愛音樂！🇭🇰", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun simpleStringLiteralWithEscapes() {
        val interpreter = interpreter("""
            val s: String = "abcdef\n\tghi \t"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("abcdef\n\tghi \t", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun simpleStringLiteralWithDollarEscape() {
        val interpreter = interpreter("""
            val s: String = "abc\${'$'}def\\\n\tghi \t"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("abc\$def\\\n\tghi \t", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun stringFieldReference() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String = "abc${'$'}x.def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abc30.def", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun stringInterpolationSimpleExpression() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String = "abc${'$'}{x * 2 + 1}def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abc61def", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun stringInterpolationIfExpression() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String = "abc${'$'}{if (x < 20) { "smaller" } else { "greater" } }def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abcgreaterdef", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun nestedStringInterpolation() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String = "abc${'$'}{if (x < 20) { "smaller${'$'}{x + 10}" } else { "great${'$'}{ if (x >= 30) { "--${'$'}{"<${'$'}{x * 2}>"}--" } else "mid${'$'}x" }er" } }def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abcgreat--<60>--erdef", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun stringConcatOperator() {
        val interpreter = interpreter("""
            val s: String = "abc" + "def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("abcdef", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun stringConcatWithInt() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String = "abc" + x + "def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abc30def", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun nullableStringConcat1() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String? = "abc"
            val r1: String = s + x
            val r2: String = x + s
            val r3: String = s + s
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals("abc30", (symbolTable.findPropertyByDeclaredName("r1") as StringValue).value)
        assertEquals("30abc", (symbolTable.findPropertyByDeclaredName("r2") as StringValue).value)
        assertEquals("abcabc", (symbolTable.findPropertyByDeclaredName("r3") as StringValue).value)
    }

    @Test
    fun nullableStringConcat2() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String? = null
            val r1: String = s + x
            val r2: String = x + s
            val r3: String = s + s
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals("null30", (symbolTable.findPropertyByDeclaredName("r1") as StringValue).value)
        assertEquals("30null", (symbolTable.findPropertyByDeclaredName("r2") as StringValue).value)
        assertEquals("nullnull", (symbolTable.findPropertyByDeclaredName("r3") as StringValue).value)
    }

    @Test
    fun nullConcatNull() {
        val interpreter = interpreter("""
            val s: String = null + null
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("nullnull", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }
}
