package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import com.sunnychung.lib.multiplatform.kotlite.error.ExpectTokenMismatchException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TryCatchFinallyErrorTest {

    @Test
    fun catchUnthrowableType() {
        assertSemanticFail("""
            class A
            try {
                throw Throwable("some error")
            } catch (e: A) {
            }
        """.trimIndent())
    }

    @Test
    fun tryWithoutCatchFinally() {
        assertFailsWith<ExpectTokenMismatchException> {
            semanticAnalyzer("""
                try {
                    throw Throwable("some error")
                }
            """.trimIndent()).analyze()
        }
    }

    @Test
    fun tryCatchExpressionWrongType() {
        assertSemanticFail("""
            val x: Int = try {
                throw Throwable()
                12
            } catch (_: Throwable) {
                "abc"
            }
        """.trimIndent())
    }
}
