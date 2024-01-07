package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class TypeInferenceErrorTest {

    @Test
    fun lambdaAsFunctionArgumentWithSomeParameterInConflict() {
        assertSemanticFail("""
            fun f(g: (Int, String) -> String): String {
                return g(1, "abc")
            }
            f { x, y: Int -> "${'$'}x ${'$'}y" }
        """.trimIndent())
    }

    @Test
    fun lambdaAsFunctionArgumentWithMissingParameter() {
        assertSemanticFail("""
            fun f(g: (Int, String) -> String): String {
                return g(1, "abc")
            }
            f { x -> "${'$'}x" }
        """.trimIndent())
    }

    @Test
    fun lambdaAsFunctionArgumentWithExtraParameter() {
        assertSemanticFail("""
            fun f(g: (Int, String) -> String): String {
                return g(1, "abc")
            }
            f { x, y, z -> "${'$'}x ${'$'}y" }
        """.trimIndent())
    }

    @Test
    fun lambdaAsFunctionArgumentWithIncorrectReturnType() {
        assertSemanticFail("""
            fun f(g: (Int, String) -> String): String {
                return g(1, "abc")
            }
            f { x, y -> x }
        """.trimIndent())
    }

    @Test
    fun nestedLambdaArgumentIncorrect() {
        assertSemanticFail("""
            fun f(a: Int, b: Int, g: (Int, Int, ((Int) -> Int)) -> Int): Int {
                return g(a, b) { x -> -x }
            }
            val a = f(10, 15) { a, b, g -> g("a + b") }
            val b = f(20, 29) { a, b, g -> g("a * b") }
        """.trimIndent())
    }

    @Test
    fun nestedLambdaReturnTypeIncorrect() {
        assertSemanticFail("""
            fun f(a: Int, b: Int, g: (Int, Int) -> ((Int) -> ((Int) -> Int))): Int {
                return g(a, b)(4)(1)
            }
            val a = f(10, 15) { a, b -> {c -> {_ -> "a + b + c"}} }
            val b = f(20, 29) { a, b -> {c -> {_ -> "a * b - c"}} }
        """.trimIndent())
    }
}
