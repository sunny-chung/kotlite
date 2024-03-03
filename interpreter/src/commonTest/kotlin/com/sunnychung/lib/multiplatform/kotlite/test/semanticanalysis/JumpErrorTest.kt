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

    @Test
    fun returnToNonExistingLabel1() {
        assertSemanticFail("""
            val f = def@ {
                return@abc 123
            }
        """.trimIndent())
    }

    @Test
    fun returnToNonExistingLabel2() {
        assertSemanticFail("""
            val f = {
                return@abc 123
            }
        """.trimIndent())
    }

    @Test
    fun returnToNonExistingLabel3() {
        assertSemanticFail("""
            fun f() = {
                return@abc 123
            }
        """.trimIndent())
    }

    @Test
    fun returnToOuterScopeLabel() {
        assertSemanticFail("""
            fun f() = abc@ {
                {
                    return@abc 123
                }()
            }
        """.trimIndent())
    }

    @Test
    fun returnWrongTypeInLambda() {
        assertSemanticFail("""
            val f: () -> Int = {
                3.5
            }
        """.trimIndent())
    }

    @Test
    fun returnWrongTypeInLambdaWithReturnLabel() {
        assertSemanticFail("""
            val f: () -> Int = a@ {
                return@a 3.5
            }
        """.trimIndent())
    }
}
