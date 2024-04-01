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

    @Test
    fun safeCallNullabilityMismatch() {
        assertSemanticFail("""
            abstract class Base(val x: Int)
            class A(x: Int, val a: Int) : Base(x)
            class B(x: Int, val b: Int) : Base(x)
            fun <T : Base> T.unwrap(f: T.() -> Int): Int = this.f()
            fun f(x: Int) = if (x > 0) A(6, 20) else null
            val a: Int = f(1)?.unwrap { x + a } // the type of `a` should be `Int?`
        """.trimIndent())
    }
}
