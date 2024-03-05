package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class LoopErrorTest {
    @Test
    fun forLoopNonIterable() {
        assertSemanticFail("""
            var x = 0
            for (i in 123) {
                ++x
            }
        """.trimIndent())
    }
}
