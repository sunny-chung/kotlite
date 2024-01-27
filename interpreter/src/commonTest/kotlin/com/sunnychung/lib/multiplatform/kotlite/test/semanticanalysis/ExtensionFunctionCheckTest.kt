package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class ExtensionFunctionCheckTest {

    @Test
    fun duplicateExtensionFunction() {
        assertSemanticFail("""
            class A
            fun A.duplicated(): Int = 10
            fun A.duplicated(): Int = 20
        """.trimIndent())
    }

    @Test
    fun duplicateGenericExtensionFunction1() {
        assertSemanticFail("""
            class A<T>
            fun A<*>.duplicated(): Int = 10
            fun A<*>.duplicated(): Int = 20
        """.trimIndent())
    }

    @Test
    fun duplicateGenericExtensionFunction2() {
        assertSemanticFail("""
            class A<T>
            fun <X> A<X>.duplicated(): Int = 10
            fun <X> A<X>.duplicated(): Int = 20
        """.trimIndent())
    }

    @Test
    fun duplicateGenericExtensionFunction3() {
        assertSemanticFail("""
            class A<T>
            fun A<String>.duplicated(): Int = 10
            fun A<String>.duplicated(): Int = 20
        """.trimIndent())
    }
}
