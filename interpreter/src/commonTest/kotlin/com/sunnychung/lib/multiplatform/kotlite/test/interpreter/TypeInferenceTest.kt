package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis.assertSemanticFail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypeInferenceTest {

    @Test
    fun propertyInt() {
        val interpreter = interpreter("""
            val a = 3
            val b = 1 + 4
            var c = a + b
            val d = ++c
            var e = d + 10
            var f = a
            e += 6
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(9, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(9, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun propertyDouble() {
        val interpreter = interpreter("""
            val a = 3.0
            val b = 2.3 * 4.5
            var c = a + b
            val d = ++c
            var e = d + 10
            var f = a
            e += 6
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        compareNumber(3.0, symbolTable.findPropertyByDeclaredName("a") as DoubleValue)
        compareNumber(10.35, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        compareNumber(14.35, symbolTable.findPropertyByDeclaredName("c") as DoubleValue)
        compareNumber(14.35, symbolTable.findPropertyByDeclaredName("d") as DoubleValue)
        compareNumber(30.35,symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
        compareNumber(3.0, symbolTable.findPropertyByDeclaredName("f") as DoubleValue)
    }

    @Test
    fun propertyString() {
        val interpreter = interpreter("""
            val a = "abc"
            val b = "ab" + 7
            var c = a + b
            val d = c + "def"
            var e = d + true
            var f = a
            e += null
            e += "qwer"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("ab7", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("abcab7", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("abcab7def", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
        assertEquals("abcab7deftruenullqwer", (symbolTable.findPropertyByDeclaredName("e") as StringValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("f") as StringValue).value)
    }

    @Test
    fun propertyBoolean() {
        val interpreter = interpreter("""
            val a = true
            val b = true && false
            var c = b || a
            val d = true || c
            var e = c && ((a && b) || b || false)
            var f = a
            e = (e && false) || true
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
    }

    @Test
    fun propertyNothing() {
        val interpreter = interpreter("""
            val a = null
            val b = a == null
            val c = null != a
            val d = a + a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is NullValue)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals("nullnull", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    fun propertyObject() {
        val interpreter = interpreter("""
            class A(val x: Int)
            val a = A(10)
            val b = A(20)
            val c = a.x
            val d = b.x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("b") is ClassInstance)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun propertyInferFromFunctionResult() {
        val interpreter = interpreter("""
            fun f(x: Int): Int {
                return 2 * x
            }
            
            val a = f(2)
            var b = f(a)
            val c = a * b
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(32, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun propertyLambda() {
        val interpreter = interpreter("""
            val f1 = { x: Int -> 2 * x }
            val f2 = { x: Double -> 1.2 * x }
            val f3 = { x: Double -> null }
            val f4 = { x: Double -> x + "def" }
            var a = f1(10)
            var b = f2(1.2)
            val c = f3(2.345)
            val d = f4(2.345)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        compareNumber(1.44, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        assertTrue(symbolTable.findPropertyByDeclaredName("c") is NullValue)
        assertEquals("2.345def", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    fun propertyChainedCalls() {
        val interpreter = interpreter("""
            fun f(): () -> (() -> (() -> Int)) {
                return {{{29}}}
            }
            val a = f()()()()
            val b = a + 25
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(54, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun lambdaArguments() {
        val interpreter = interpreter("""
            fun f(a: Int, b: Int, g: (Int, Int) -> Int): Int {
                return g(a, b)
            }
            val a = f(10, 15) { a, b -> a + b }
            val b = f(20, 29) { a, b -> a * b }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(580, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun nestedLambdaArguments() {
        val interpreter = interpreter("""
            fun f(a: Int, b: Int, g: (Int, Int, ((Int) -> Int)) -> Int): Int {
                return g(a, b) { x -> -x }
            }
            val a = f(10, 15) { a, b, g -> g(a + b) }
            val b = f(20, 29) { a, b, g -> g(a * b) }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(-25, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-580, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun nestedLambdaReturnType1() {
        val interpreter = interpreter("""
            fun f(a: Int, b: Int, g: (Int, Int) -> ((Int) -> Int)): Int {
                return g(a, b)(4)
            }
            val a = f(10, 15) { a, b -> {c -> a + b + c} }
            val b = f(20, 29) { a, b -> {c -> a * b - c} }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(576, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun nestedLambdaReturnType2() {
        val interpreter = interpreter("""
            fun f(a: Int, b: Int, g: (Int, Int) -> ((Int) -> ((Int) -> Int))): Int {
                return g(a, b)(4)(1)
            }
            val a = f(10, 15) { a, b -> {c -> {_ -> a + b + c}} }
            val b = f(20, 29) { a, b -> {c -> {_ -> a * b - c}} }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(576, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun propertyLambdaParameters() {
        val interpreter = interpreter("""
            val f: (Int, String) -> String = { i, s ->
                s + i
            }
            val a = f(1, "a")
            val b = f(2, "b")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("a1", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("b2", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun propertyNullableLambdaParameters() {
        val interpreter = interpreter("""
            val f: ((Int, String) -> String)? = { i, s ->
                s + i
            }
            val a = f!!(1, "a")
            val b = f!!(2, "b")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("a1", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("b2", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun propertyNestedLambdaParameters() {
        val interpreter = interpreter("""
            val f: (Int, String) -> ((Int) -> String) = { i, s ->
                { x -> x + s + i }
            }
            val a = f(1, "a")(3)
            val b = f(2, "b")(4)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("3a1", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("4b2", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun assignNullableLambdaParameters() {
        val interpreter = interpreter("""
            var f: ((Int, String) -> String)? = null
            f = null
            f = { i, s ->
                s + i
            }
            val a = f!!(1, "a")
            val b = f!!(2, "b")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("a1", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("b2", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun returnLambda() {
        val interpreter = interpreter("""
            fun f(): (Int, String) -> String {
                return { i, s ->
                    s + i
                }
            }
            val a = f()(1, "a")
            val b = f()(2, "b")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("a1", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("b2", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun functionBlockDoesNotInferReturnType() {
        assertSemanticFail("""
            fun f() {
                3
            }
            val a = f()
            val b = a + 1
        """.trimIndent())
    }

    @Test
    fun functionReturnsInt() {
        val interpreter = interpreter("""
            fun f() = 3
            fun g(a: Int) = 4 + a
            val a = f()
            val b = g(a)
            val c = g(b) + f()
            val d = f() + g(b)
            val e = 1 - g(b)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(7, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-10, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun functionReturnsDouble() {
        val interpreter = interpreter("""
            fun f() = 3.14
            fun g(a: Double) = 1 + a
            val a = f()
            val b = g(a)
            val c = g(b) + f()
            val d = f() + g(b)
            val e = 1 - g(b)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        compareNumber(3.14, symbolTable.findPropertyByDeclaredName("a") as DoubleValue)
        compareNumber(4.14, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        compareNumber(8.28, symbolTable.findPropertyByDeclaredName("c") as DoubleValue)
        compareNumber(8.28, symbolTable.findPropertyByDeclaredName("d") as DoubleValue)
        compareNumber(-4.14, symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
    }

    @Test
    fun functionReturnsString() {
        val interpreter = interpreter("""
            fun f() = "abcd"
            fun g(a: Int) = "efgh" + a
            val a = f()
            val b = g(2)
            val c = g(3) + f()
            val d = f() + g(3)
            val e = f() + "xyz"
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals("abcd", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("efgh2", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("efgh3abcd", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("abcdefgh3", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
        assertEquals("abcdxyz", (symbolTable.findPropertyByDeclaredName("e") as StringValue).value)
    }

    @Test
    fun functionReturnsBoolean() {
        val interpreter = interpreter("""
            fun f() = false
            fun g(a: Int) = a > 0
            val a = f()
            val b = g(2)
            val c = g(-3)
            val d = f() || g(3)
            val e = f() && g(4)
            val f = !f() && (false || f() || f() || true)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
    }

    @Test
    fun functionReturnsNothing() {
        val interpreter = interpreter("""
            fun f() = null
            fun g(a: Int) = if (a > 0) null else null
            val a = f()
            val b = g(10)
            val c = f() + null
            val d = f() + g(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is NullValue)
        assertTrue(symbolTable.findPropertyByDeclaredName("b") is NullValue)
        assertEquals("nullnull", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("nullnull", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    fun functionReturnsObject() {
        val interpreter = interpreter("""
            class A(val x: Int)
            fun f() = A(1)
            fun g(a: Int) = A(a)
            val a = f()
            val b = g(10)
            val c = f().x + g(20).x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("b") is ClassInstance)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun functionReturnsNullableObject() {
        val interpreter = interpreter("""
            class A(val x: Int)
            fun f() = A(1)
            fun g(a: Int) = if (a > 0) {
                A(a)
            } else {
                null
            }
            val a = f()
            val b = g(10)
            val c = g(-10)
            val d = f().x + g(20)!!.x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("b") is ClassInstance)
        assertTrue(symbolTable.findPropertyByDeclaredName("c") is NullValue)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun functionReturnsLambdaErrorCase() {
        assertSemanticFail("""
            class A(val x: Int)
            fun f() = { a: Int ->
                if (a > 0) {
                    A(a)
                } else {
                    null
                }
            }
            val a = f()
            val b = a(10)
            val c = b.x
        """.trimIndent())
    }

    @Test
    fun functionReturnsLambda() {
        val interpreter = interpreter("""
            class A(val x: Int)
            fun f() = { a: Int ->
                if (a > 0) {
                    A(a)
                } else {
                    null
                }
            }
            val a = f()
            val b = a(10)
            val c = b!!.x
            val d = a(-10)
            val e = f()(20)!!.x
            val f = f()(-20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is LambdaValue)
        assertTrue(symbolTable.findPropertyByDeclaredName("b") is ClassInstance)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertTrue(symbolTable.findPropertyByDeclaredName("d") is NullValue)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertTrue(symbolTable.findPropertyByDeclaredName("f") is NullValue)
    }

    @Test
    fun functionReturnsNullableLambdaErrorCase() {
        assertSemanticFail("""
            class A(val x: Int)
            fun f(x: Int) = if (x > 0) {
                { a: Int ->
                    if (a > 0) {
                        A(a)
                    } else {
                        null
                    }
                }
            } else null
            val a = f(1)
            val b = a(10)
        """.trimIndent())
    }

    @Test
    fun functionReturnsNullableLambda() {
        val interpreter = interpreter("""
            class A(val x: Int)
            fun f(x: Int) = if (x > 0) {
                { a: Int ->
                    if (a > 0) {
                        A(a)
                    } else {
                        null
                    }
                }
            } else null
            val a = f(1)
            val b = a!!(10)
            val c = b!!.x
            val d = a!!(-10)
            val e = f(1)!!(20)!!.x
            val f = f(1)!!(-20)
            val g = f(-1)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is LambdaValue)
        assertTrue(symbolTable.findPropertyByDeclaredName("b") is ClassInstance)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertTrue(symbolTable.findPropertyByDeclaredName("d") is NullValue)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertTrue(symbolTable.findPropertyByDeclaredName("f") is NullValue)
        assertTrue(symbolTable.findPropertyByDeclaredName("g") is NullValue)
    }
}
