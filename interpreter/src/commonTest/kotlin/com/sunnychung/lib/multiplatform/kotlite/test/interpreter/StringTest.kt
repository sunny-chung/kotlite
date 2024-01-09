package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
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
            val s: String = "abc${'$'}{
                if (x < 20) {
                    "smaller${'$'}{x + 10}"
                } else {
                    "great${'$'}{ if (x >= 30) {
                        "--${'$'}{
                            "${'$'}x<${'$'}{x * 2}>"
                        }--"
                    } else "mid${'$'}x" }er"
                }
            }def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abcgreat--30<60>--erdef", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
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

    @Test
    fun stringPlusAssignOperator1() {
        val interpreter = interpreter("""
            var s: String = "abc"
            s += "def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("abcdef", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun stringPlusAssignOperator2() {
        val interpreter = interpreter("""
            val x: Double = 30.2
            var s: String? = "abc"
            s += x
            s += 2 > 1
            s += null
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abc30.2truenull", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun multilineStringLiteral() {
        val interpreter = interpreter("""
            val s: String = ${"\"\"\""}
            abcd
            ef
            ${"\"\"\""}
        """.trimIndent().trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("\nabcd\nef\n", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun multilineStringUnicodeLiteral() {
        val interpreter = interpreter("""
            val s: String = ${"\"\"\""}
            我愛音樂！
            🇭🇰
            ${"\"\"\""}
        """.trimIndent().trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("\n我愛音樂！\n🇭🇰\n", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun multilineStringFieldReference() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String = ${"\"\"\""}
            abc${'$'}x.de
            f
            ${"\"\"\""}
        """.trimIndent().trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("\nabc30.de\nf\n", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun multilineStringInterpolationExpression() {
        val interpreter = interpreter("""
            val s: String? = ${"\"\"\""}
            abc${'$'}{
                if (true) {
                    var i: Int = 10
                    var s: String = ""
                    while (i >= 0) {
                        s += "${'$'}{
                            "${'$'}{i * 2}\n"
                        }"
                        --i
                    }
                    s
                } else ""
            }def${"\"\"\""}
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals("\nabc20\n18\n16\n14\n12\n10\n8\n6\n4\n2\n0\ndef", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun multilineStringNestedInterpolation() {
        val interpreter = interpreter("""
            val x: Int = 30
            val s: String = "abc${'$'}{
                if (x < 20) {
                    "smaller${'$'}{x + 10}"
                } else {
                    "great${'$'}{ if (x >= 30) {
                        "--${'$'}{
                            "${'$'}x<${'$'}{x * 2}>"
                        }--"
                    } else "mid${'$'}x" }er"
                }
            }def"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("abcgreat--30<60>--erdef", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun compareStrings() {
        val interpreter = interpreter("""
            val a1 = "abcde" < "aaaaa"
            val b1 = "abcde" > "aaaaa"
            val c1 = "abcde" <= "aaaaa"
            val d1 = "abcde" >= "aaaaa"
            val a2 = "abcde" < "abcde"
            val b2 = "abcde" > "abcde"
            val c2 = "abcde" <= "abcde"
            val d2 = "abcde" >= "abcde"
            val a3 = "abcde" < "aaaaaaaaaaaaa"
            val b3 = "abcde" > "aaaaaaaaaaaaa"
            val c3 = "abcde" <= "aaaaaaaaaaaaa"
            val d3 = "abcde" >= "aaaaaaaaaaaaa"
            val a4 = "abcde" < "ba"
            val b4 = "abcde" > "ba"
            val c4 = "abcde" <= "ba"
            val d4 = "abcde" >= "ba"
            val e1 = "abcde" == "abcde"
            val f1 = "abcde" != "abcde"
            val e2 = "abcde" == "abcd"
            val f2 = "abcde" != "abcd"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(20, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a1") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c1") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a2") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b2") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c2") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d2") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a3") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b3") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c3") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d3") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a4") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b4") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c4") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d4") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("f1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e2") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f2") as BooleanValue).value)
    }
}
