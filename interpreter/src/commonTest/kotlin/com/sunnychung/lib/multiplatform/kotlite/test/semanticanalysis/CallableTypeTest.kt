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
    fun unmatchedReturnType() {
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
}