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
    fun equalsWithIncorrectValueParameterType3() {
        assertSemanticFail("""
            class A {
                override fun equals(other: Any?, o: Int) = true
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

    @Test
    fun hashCodeWithoutOverride() {
        assertSemanticFail("""
            class A {
                fun hashCode(): Int = 1
            }
        """.trimIndent())
    }

    @Test
    fun hashCodeWithIncorrectValueParameterType() {
        assertSemanticFail("""
            class A {
                override fun hashCode(o: Any): Int = 1
            }
        """.trimIndent())
    }

    @Test
    fun hashCodeWithIncorrectReturnType() {
        assertSemanticFail("""
            class A {
                override fun hashCode(): Long = 1L
            }
        """.trimIndent())
    }

    @Test
    fun toStringWithoutOverride() {
        assertSemanticFail("""
            class A {
                fun toString(): String = "1"
            }
        """.trimIndent())
    }

    @Test
    fun toStringWithIncorrectValueParameterType() {
        assertSemanticFail("""
            class A {
                override fun toString(o: Any): String = "1"
            }
        """.trimIndent())
    }

    @Test
    fun toStringWithIncorrectReturnType() {
        assertSemanticFail("""
            class A {
                override fun toString(): Long = 1L
            }
        """.trimIndent())
    }
}
