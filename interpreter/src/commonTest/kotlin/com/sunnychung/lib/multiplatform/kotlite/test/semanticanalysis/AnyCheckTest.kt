package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class AnyCheckTest {

    @Test
    fun equalsWithoutOverride() {
        assertSemanticFail("""
            class A {
                fun equals(o: Any?) = true
            }
        """.trimIndent())
    }

    @Test
    fun equalsWithIncorrectValueParameterType1() {
        assertSemanticFail("""
            class A {
                override fun equals(o: Any) = true
            }
        """.trimIndent())
    }

    @Test
    fun equalsWithIncorrectValueParameterType2() {
        assertSemanticFail("""
            class A {
                override fun equals(o: A?) = true
            }
        """.trimIndent())
    }

    @Test
    fun equalsWithIncorrectReturnType() {
        assertSemanticFail("""
            class A {
                override fun equals(o: Any?): Int = 1
            }
        """.trimIndent())
    }
}
