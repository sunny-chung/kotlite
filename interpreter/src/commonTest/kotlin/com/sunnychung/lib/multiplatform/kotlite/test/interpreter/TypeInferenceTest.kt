package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.error.CannotInferTypeException
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis.assertSemanticFail
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun classMemberProperty() {
        val interpreter = interpreter("""
            class A {
                var a = 1
            }
            val o = A()
            val x = o.a
            o.a += 2
            val y = o.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun classMemberFunction() {
        val interpreter = interpreter("""
            class A {
                var a = 1
                fun getA() = a
            }
            val o = A()
            val x = o.a
            o.a += 2
            val y = o.getA()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun inferGenericClass() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            val p1 = MyPair(10, "abc")
            val a1 = p1.first
            val b1 = p1.second
            val p2 = MyPair("def", 2.45)
            val a2 = p2.first
            val b2 = p2.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b1") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a2") as StringValue).value)
        compareNumber(2.45, symbolTable.findPropertyByDeclaredName("b2") as DoubleValue)
    }

    @Test
    fun inferGenericClassWithNamedArguments() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            val p1 = MyPair(second = "abc", first = 10)
            val a1 = p1.first
            val b1 = p1.second
            val p2 = MyPair(first = "def", second = 2.45)
            val a2 = p2.first
            val b2 = p2.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b1") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a2") as StringValue).value)
        compareNumber(2.45, symbolTable.findPropertyByDeclaredName("b2") as DoubleValue)
    }

    @Test
    fun inferNestedGenericClass() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            class MyVal<T>(val value: T)
            val p1 = MyPair(MyVal(10), MyVal(MyVal("abc")))
            val a1 = p1.first.value
            val b1 = p1.second.value.value
            val p2 = MyPair(MyPair("def", MyVal(MyPair(2, MyVal("abc")))), 2.45)
            val a2 = p2.first.first
            val b2 = p2.first.second.value.first
            val c2 = p2.first.second.value.second.value
            val d2 = p2.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b1") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a2") as StringValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b2") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("c2") as StringValue).value)
        compareNumber(2.45, symbolTable.findPropertyByDeclaredName("d2") as DoubleValue)
    }

    @Test
    fun genericFunction1() {
        val interpreter = interpreter("""
            fun <T> identity(a: T): T = a
            val a = identity(10)
            val x = "abc"
            val b = identity(a = x)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun genericFunction2() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            fun <A, B> extractFirst(p: MyPair<A, B>): A = p.first
            fun <A, B> extractSecond(p: MyPair<A, B>) = p.second
            val p1 = MyPair(second = "abc", first = 10)
            val a1 = extractFirst(p1)
            val b1 = extractSecond(p1)
            val p2 = MyPair(first = "def", second = 2.45)
            val a2 = extractFirst(p2)
            val b2 = extractSecond(p2)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b1") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a2") as StringValue).value)
        compareNumber(2.45, symbolTable.findPropertyByDeclaredName("b2") as DoubleValue)
    }

    @Test
    fun genericFunctionWithLambdaArgumentContainingTypeParameters() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            fun <A, B> extractFirst(p: MyPair<A, B>, op: (MyPair<A, B>) -> A): A = op(p)
            fun <A, B> extractSecond(p: MyPair<A, B>, op: (MyPair<A, B>) -> B) = op(p)
            val p1 = MyPair(second = "abc", first = 10)
            val a1 = extractFirst(p1) { it -> it.first }
            val b1 = extractSecond(p1) { it -> it.second }
            val p2 = MyPair(first = "def", second = "ghi")
            val a2 = extractFirst(p2) { it -> it.second } // note the lambda is intentionally reversed in order to test lambda is working
            val b2 = extractSecond(p2) { it -> it.first } // note the lambda is intentionally reversed in order to test lambda is working
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b1") as StringValue).value)
        assertEquals("ghi", (symbolTable.findPropertyByDeclaredName("a2") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("b2") as StringValue).value)
    }

    @Test
    fun genericFunctionWithNestedTypeParameters() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            class MyVal<T>(val value: T)
            fun <A, B> extractFirst(p: MyPair<A, B>): A = p.first
            fun <A, B> extractSecond(p: MyPair<A, B>): B = p.second
            fun <A> extractValue(v: MyVal<A>): A = v.value
            val p1 = MyPair(MyVal(10), MyVal(MyVal("abc")))
            val a1 = extractValue(p1.first)
            val b1 = extractValue(extractValue(extractSecond(p1)))
            val p2 = MyPair(MyPair("def", MyVal(MyPair(2, MyVal("abc")))), 2.45)
            val a2 = extractFirst(extractFirst(p2))
            val b2 = extractFirst(extractValue(extractSecond(extractFirst(p2))))
            val c2 = extractValue(extractSecond(extractValue(extractSecond(extractFirst(p2)))))
            val d2 = extractSecond(p2)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(8, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b1") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a2") as StringValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b2") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("c2") as StringValue).value)
        compareNumber(2.45, symbolTable.findPropertyByDeclaredName("d2") as DoubleValue)
    }

    @Test
    fun functionWithClassNameIsSameAsTypeParameterNameNested() {
        assertFailsWith<CannotInferTypeException> { // TODO to be fixed
                val interpreter = interpreter("""
                class T<T>(var value: T) {
                    fun getValue(): T = value
                    fun func(op: () -> T): T = op()
                    fun asT(): T = 20 as T
                }
                fun <A> extractValue(v: T<A>): A = v.getValue()
                val o = T(T(T(10)))
    //            extractValue(o)
    //            val a = extractValue(extractValue(o)).func { 15 }
    //            val b = extractValue(extractValue(extractValue(o)))
    //            val c = extractValue(extractValue(o.getValue()))
    //            val d = extractValue(extractValue(o)).asT()
            """.trimIndent())
            interpreter.eval()
            val symbolTable = interpreter.callStack.currentSymbolTable()
            assertEquals(5, symbolTable.propertyValues.size)
            println(symbolTable.propertyValues)
            assertTrue(symbolTable.findPropertyByDeclaredName("o") is ClassInstance)
            assertEquals(
                "T<T<T<Int>>>",
                (symbolTable.findPropertyByDeclaredName("o") as ClassInstance).type().descriptiveName
            )
            assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
            assertEquals(10, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
            assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
            assertEquals(20, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        }
    }

    @Test
    fun extensionFunctionUseUnrelatedTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun <T, X> MyVal1<X>.identity(value: T): T = value
            val o = MyVal1("def")
            val a: String = o.getValue()
            val b: Int = o.identity(20)
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
            val o = MyVal1("def")
            val p = o.toPair(20)
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
            val o = MyVal1("def")
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
            val o = MyVal1("def")
            val a: String = o.getValue()
            val b: Int = o.identity(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseLambda() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun <T, R> MyVal1<T>.map(op: (T) -> R) = MyVal1(op(value))
            val o = MyVal1("def")
            val a = o.map { it -> "abc" + it }
            val av = a.value
            val b = a.map { _ -> 20 }
            val bv = b.getValue()
            val c = b.map { it -> it * 4.5 }
            val cv = c.getValue()
            val d = c.map { it -> MyPair("new", it) }
            val dv1 = d.value.first
            val dv2 = d.value.second
            val e = d.map { it -> MyPair(it.first + " pair", it.second * 2) }
            val ev1 = e.value.first
            val ev2 = e.value.second
            val f = e.map { _ -> MyPair(123, MyPair(234, "bcd")) }
            val fv1 = f.value.first
            val fv2 = f.value.second.first
            val fv3 = f.value.second.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(17, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("MyVal1<String>", (symbolTable.findPropertyByDeclaredName("a") as ClassInstance).type().descriptiveName)
        assertEquals("abcdef", (symbolTable.findPropertyByDeclaredName("av") as StringValue).value)
        assertEquals("MyVal1<Int>", (symbolTable.findPropertyByDeclaredName("b") as ClassInstance).type().descriptiveName)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("bv") as IntValue).value)
        assertEquals("MyVal1<Double>", (symbolTable.findPropertyByDeclaredName("c") as ClassInstance).type().descriptiveName)
        compareNumber(20 * 4.5, symbolTable.findPropertyByDeclaredName("cv") as DoubleValue)
        assertEquals("MyVal1<MyPair<String, Double>>", (symbolTable.findPropertyByDeclaredName("d") as ClassInstance).type().descriptiveName)
        assertEquals("new", (symbolTable.findPropertyByDeclaredName("dv1") as StringValue).value)
        compareNumber(20 * 4.5, symbolTable.findPropertyByDeclaredName("dv2") as DoubleValue)
        assertEquals("MyVal1<MyPair<String, Double>>", (symbolTable.findPropertyByDeclaredName("e") as ClassInstance).type().descriptiveName)
        assertEquals("new pair", (symbolTable.findPropertyByDeclaredName("ev1") as StringValue).value)
        compareNumber(20 * 4.5 * 2, symbolTable.findPropertyByDeclaredName("ev2") as DoubleValue)
        assertEquals("MyVal1<MyPair<Int, MyPair<Int, String>>>", (symbolTable.findPropertyByDeclaredName("f") as ClassInstance).type().descriptiveName)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("fv1") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("fv2") as IntValue).value)
        assertEquals("bcd", (symbolTable.findPropertyByDeclaredName("fv3") as StringValue).value)
    }

    @Test
    fun nestedGenericsInClassTypeParameters() {
        val interpreter = interpreter("""
            open class A<T>(v: T) {
                var value: T? = null
                
                init {
                    value = v
                }
            }
            class MyPair<T1, T2>(val first: T1, val second: T2)
            class B<T1, T2>(x: MyPair<T1, T2>) : A<MyPair<T1, T2>>(x)
            val x = B(MyPair(123, "abc"))
            val y = B(MyPair("def", 456))
            val a: Int = x.value!!.first
            val b: String = x.value!!.second
            val c: String = y.value!!.first
            val d: Int = y.value!!.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(456, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }
}
