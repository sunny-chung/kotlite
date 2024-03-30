package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class FunctionTypeWithReceiverCheckTest {
    @Test
    fun varargParameterTypeCannotHaveReceiver() {
        assertSemanticFail("""
            fun f(vararg value: Int.() -> Unit) {
                // no-op
            }
        """.trimIndent())
    }

    @Test
    fun functionLiteralWithReceiverCannotBePropagatedToAnotherFunctionCall() {
        assertSemanticFail("""
            fun f(operation: Int.() -> Int): Int {
                return 10.operation()
            }
            val x = f {
                this.operation() * 12
            }
        """.trimIndent())
    }
}
