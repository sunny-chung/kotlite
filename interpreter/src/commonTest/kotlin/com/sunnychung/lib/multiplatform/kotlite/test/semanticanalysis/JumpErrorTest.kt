package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class JumpErrorTest {

    @Test
    fun throwNonThrowable() {
        assertSemanticFail("""
            throw 1
        """.trimIndent())
    }
}
