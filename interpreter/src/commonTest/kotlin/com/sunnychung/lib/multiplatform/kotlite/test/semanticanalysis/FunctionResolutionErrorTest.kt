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

    @Test
    fun invalidSubjectGet() {
        assertSemanticFail("""
            class IntPair(var first: Int, val second: Int) {
                operator fun get(index: Int): Int {
                    return first
                }
                operator fun set(index: Int, value: Int) {
                    first = value
                }
            }
            val x = abc[0]
        """.trimIndent())
    }

    @Test
    fun invalidSubjectSet() {
        assertSemanticFail("""
            class IntPair(var first: Int, val second: Int) {
                operator fun get(index: Int): Int {
                    return first
                }
                operator fun set(index: Int, value: Int) {
                    first = value
                }
            }
            abc[0] = 2
        """.trimIndent())
    }

    @Test
    fun nullExtensionMethods() {
        assertSemanticFail("""
            fun Int?.happyNumber(): Int = 5
            fun String?.happyNumber(): Int = 6
            val x: Int = null.happyNumber()
        """.trimIndent())
    }
}
