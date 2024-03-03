package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class JumpErrorTest {

    @Test
    fun throwNonThrowable() {
        assertSemanticFail("""
            throw 1
        """.trimIndent())
    }

    @Test
    fun directReturnFromLambdaCreatedInsideFunctions() {
        assertSemanticFail("""
            fun f() = { x: Int ->
                if (x < 10) {
                    return 3
                }
                1
            }
            fun g(x: Int) = f()(x)
            val a = g(2)
        """.trimIndent())
    }

    @Test
    fun directReturnFromLambdaCreatedInGlobal() {
        assertSemanticFail("""
            val f = { x: Int ->
                if (x < 10) {
                    return 3
                }
                1
            }
            fun g(x: Int) = f(x)
            val a = g(2)
        """.trimIndent())
    }
}
