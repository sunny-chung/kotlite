package com.sunnychung.lib.multiplatform.kotlite.error

import com.sunnychung.lib.multiplatform.kotlite.model.ThrowableValue

open class EvaluateRuntimeException(val stacktrace: List<String>, val error: ThrowableValue) : Exception(error.message) {
    fun printWithStacktrace() {
        val fullMessage = buildString {
            append(error.message)
            append(" at ")
            stacktrace.forEach {
                appendLine(it)
            }
        }
        println(fullMessage)
    }
}
