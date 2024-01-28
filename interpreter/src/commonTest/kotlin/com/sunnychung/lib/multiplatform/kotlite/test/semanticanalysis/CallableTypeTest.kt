package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class CallableTypeTest {

    @Test
    fun invalidCallable1() {
        assertSemanticFail("""
            val a: Int = 10
            a(10)
        """.trimIndent())
    }

    @Test
    fun invalidCallable2() {
        assertSemanticFail("""
            val a: String = "a"
            a(10)
        """.trimIndent())
    }

    @Test
    fun invalidCallable3() {
        assertSemanticFail("""
            val a: Int? = null
            a(10)
        """.trimIndent())
    }

    @Test
    fun invalidChainCallable1() {
        assertSemanticFail("""
            val a: (Int) -> Int = { i: Int -> i }
            a(10)(10)
        """.trimIndent())
    }

    @Test
    fun invalidChainCallable2() {
        assertSemanticFail("""
            val a: (Int) -> Int = { i: Int -> i }
            a(10)()
        """.trimIndent())
    }

    @Test
    fun invalidChainCallable3() {
        assertSemanticFail("""
            val a: (Int) -> Unit = { i: Int -> i }
            a(10)(10)
        """.trimIndent())
    }

    @Test
    fun invalidChainCallable4() {
        assertSemanticFail("""
            val a: (Int) -> Unit = { i: Int -> i }
            a(10)()
        """.trimIndent())
    }

    @Test
    fun unmatchedReturnType1() {
        assertSemanticFail("""
            fun f(x: Int): ((Int) -> Int) -> Int {
                return { g: (Int) -> Int ->
                    f(2 * x)
                }
            }
            val b: ((Int) -> Int) -> Int = f(10)
            val c: Int = b({x: Int -> 2*x})
        """.trimIndent())
    }

    @Test
    fun unmatchedReturnType2() {
        assertSemanticFail("""
            fun f(): () -> (() -> (() -> Unit)) {
                return {{{29}}}
            }
            val a = f()()()()
        """.trimIndent())
    }

    @Test
    fun unmatchedArgumentLambdaType1() {
        assertSemanticFail("""
            fun f(g: () -> Int): Int {
                return g()
            }
            val a: Int = f({x: Int -> 2*x})
        """.trimIndent())
    }

    @Test
    fun unmatchedArgumentLambdaType2() {
        assertSemanticFail("""
            fun f(g: () -> Int): Int {
                return g()
            }
            val a: Int = f({ "abc" })
        """.trimIndent())
    }

    @Test
    fun unmatchedArgumentLambdaType3() {
        assertSemanticFail("""
            fun f(g: () -> Int): Int {
                return g()
            }
            val a: Int = f {x: Int -> 2*x}
        """.trimIndent())
    }

    @Test
    fun unmatchedArgumentLambdaType4() {
        assertSemanticFail("""
            fun f(g: () -> Int): Int {
                return g()
            }
            val a: Int = f { "abc" }
        """.trimIndent())
    }

    @Test
    fun unmatchedArgumentCount1() {
        assertSemanticFail("""
            fun f(a: Int, b: String): Int {
                return g()
            }
            val a: Int = f(3, "abc", 4)
        """.trimIndent())
    }

    @Test
    fun unmatchedArgumentCount2() {
        assertSemanticFail("""
            fun f(a: Int, b: String): Int {
                return g()
            }
            val a: Int = f(3, b = "abc", a = 4)
        """.trimIndent())
    }

    @Test
    fun duplicatedArgument() {
        assertSemanticFail("""
            fun f(a: Int, b: String = "abc"): Int {
                return g()
            }
            val a: Int = f(3, a = 4)
        """.trimIndent())
    }

    @Test
    fun missingMandatoryArgument1() {
        assertSemanticFail("""
            fun f(a: Int, b: String = "abc"): Int {
                return g()
            }
            val a: Int = f(b = "def")
        """.trimIndent())
    }

    @Test
    fun missingMandatoryArgument2() {
        assertSemanticFail("""
            fun f(a: Int, b: String = "abc"): Int {
                return g()
            }
            val a: Int = f()
        """.trimIndent())
    }

    @Test
    fun extraArgument() {
        assertSemanticFail("""
            fun f(a: Int, b: String = "abc"): Int {
                return g()
            }
            val a: Int = f(3, c = "def")
        """.trimIndent())
    }

    @Test
    fun unmatchedArgumentType1() {
        assertSemanticFail("""
            fun f(a: Int, b: String = "abc"): Int {
                return g()
            }
            val a: Int = f(a = "def")
        """.trimIndent())
    }

    @Test
    fun unmatchedArgumentType2() {
        assertSemanticFail("""
            fun f(a: Int, b: String = "abc"): Int {
                return g()
            }
            val a: Int = f(3, 4)
        """.trimIndent())
    }

    @Test
    fun callNullableLambda1() {
        assertSemanticFail("""
            fun f(a: Int): (() -> Int)? {
                return if (a > 0) {
                    { a }
                } else {
                    null
                }
            }
            val a: Int = f(10)()
        """.trimIndent())
    }

    @Test
    fun callNullableLambda2() {
        assertSemanticFail("""
            class A {
                fun f(a: Int): (() -> Int)? {
                    return if (a > 0) {
                        { a }
                    } else {
                        null
                    }
                }
            }
            val a: Int = A().f(10)()
        """.trimIndent())
    }

    @Test
    fun callNullableLambda3() {
        assertSemanticFail("""
            fun f(a: Int): (() -> Int)? {
                return if (a > 0) {
                    { a }
                } else {
                    null
                }
            }
            val a: (() -> Int)? = f(10)
            val b: Int = a()
        """.trimIndent())
    }

    @Test
    fun callNullableLambdaSuccess() {
        semanticAnalyzer("""
            fun f(a: Int): (() -> Int)? {
                return if (a > 0) {
                    { a }
                } else {
                    null
                }
            }
            val a: (() -> Int)? = f(10)
            val b: Int = a!!()
        """.trimIndent()).analyze()
    }

    @Test
    fun varargIncompatibleArgumentType1() {
        assertSemanticFail("""
            var a = 0
            fun f(vararg args: Any) {
                ++a
            }
            f(null)
        """.trimIndent())
    }

    @Test
    fun varargIncompatibleArgumentType2() {
        assertSemanticFail("""
            var a = 0
            fun f(vararg args: String) {
                ++a
            }
            f(1, 2, 3)
        """.trimIndent())
    }

    @Test
    fun varargWithDefaultValue() {
        assertSemanticFail("""
            fun f(vararg args: Int = 1) {}
        """.trimIndent())
    }
}