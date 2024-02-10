package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class GenericFunctionCheckTest {

    @Test
    fun inconsistentGenericParameter() {
        assertSemanticFail("""
            fun <T> concat(a: T, b: T): String = "${'$'}a${'$'}b"
            val s = concat<Int>(123, 4.5)
        """.trimIndent())
    }

    @Test
    fun inconsistentGenericLambdaParameter() {
        assertSemanticFail("""
            fun <T> concat(a: T, b: T, operation: (T, T) -> String): String = operation(a, b)
            val s = concat<Int>(123, 45) { a: Double, b: Double -> "${'$'}a${'$'}b" }
        """.trimIndent())
    }

    @Test
    fun inconsistentGenericParameterAndReturnType() {
        assertSemanticFail("""
            fun <T> identity(a: T): T = a
            val s: String = identity<Int>(123)
        """.trimIndent())
    }

    @Test
    fun genericVarargWithoutArgument() {
        // even the official Kotlin compiler does not infer this case
        assertSemanticFail("""
            fun <T> f(vararg args: T) {}
            f()
        """.trimIndent())
    }

    @Test
    fun nonexistTypeArgument1() {
        assertSemanticFail("""
            fun <T> f(): String = "abc"
            val s = f<ABC>()
        """.trimIndent())
    }

    @Test
    fun nonexistTypeArgument2() {
        assertSemanticFail("""
            fun <T> f(): String = "abc"
            val s = f<T>()
        """.trimIndent())
    }
}