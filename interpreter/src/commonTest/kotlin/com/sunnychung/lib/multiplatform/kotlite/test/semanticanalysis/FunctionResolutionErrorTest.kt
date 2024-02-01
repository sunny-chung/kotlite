package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class FunctionResolutionErrorTest {

    @Test
    fun indexOperatorCannotResolveToNonOperatorFunctions() {
        assertSemanticFail("""
            class IntPair(val first: Int, val second: Int) {
                fun get(index: Int): Int {
                    return first
                }
            }
            val x = IntPair(123, 45)
            val a = x[0]
        """.trimIndent())
    }
}
