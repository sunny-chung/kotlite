package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class WhenCheckTest {
    @Test
    fun emptyWhen() {
        assertSemanticFail("""
            when {}
        """.trimIndent())
    }

    @Test
    fun nonBooleanExpressionWithoutSubject1() {
        assertSemanticFail("""
            when {
                1, 3 -> 4
                5 -> 5
                else -> 6
            }
        """.trimIndent())
    }

    @Test
    fun nonBooleanExpressionWithoutSubject2() {
        assertSemanticFail("""
            when {
                is Boolean -> 4
                is Double -> 5
                else -> 6
            }
        """.trimIndent())
    }

    @Test
    fun useCommaInExpressionWithoutSubject() {
        assertSemanticFail("""
            val x = 10
            when {
                x < 10 -> 4
                x < 15, x > 20 -> 5
                else -> 6
            }
        """.trimIndent())
    }

    @Test
    fun incorrectTypeCheck() {
        assertSemanticFail("""
            val x = 10
            val y = 10
            when (x) {
                is y -> 4
                else -> 6
            }
        """.trimIndent())
    }

    // TODO this test should be restricted to `when` being used as expressions only
    @Test
    fun whenWithoutElse() {
        assertSemanticFail("""
            val x = 3
            when (x) {
                0, 1 -> 4
                3 -> 5
            }
        """.trimIndent())
    }

    @Test
    fun whenWithManyElseBranches() {
        assertSemanticFail("""
            val x = 3
            when (x) {
                1 -> 0
                else -> 1
                else -> 2
            }
        """.trimIndent())
    }

    @Test
    fun elseIsNotTheLastBranch() {
        assertSemanticFail("""
            val x = 3
            when (x) {
                else -> 1
                1 -> 0
            }
        """.trimIndent())
    }

    @Test
    fun incorrectReturnType() {
        assertTypeCheckFail("""
            val x = 3
            val y: Int = when (x) {
                1, 2, 3 -> 1
                else -> null
            }
        """.trimIndent())
    }
}
