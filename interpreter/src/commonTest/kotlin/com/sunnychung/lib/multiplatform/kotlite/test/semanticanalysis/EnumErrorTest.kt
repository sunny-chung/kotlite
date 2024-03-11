package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class EnumErrorTest {
    @Test
    fun accessNonExistEnum() {
        assertSemanticFail("""
            enum class MyEnum {
                A, B, C, D
            }
            val e = MyEnum.E
        """.trimIndent())
    }

    @Test
    fun incorrectEnumType() {
        assertSemanticFail("""
            enum class MyEnum {
                A, B, C, D
            }
            val e: Int = MyEnum.A
        """.trimIndent())
    }

    @Test
    fun enumClassCannotInheritClass() {
        assertSemanticFail("""
            open class SomeClass
            enum class MyEnum : SomeClass() {
                A, B, C, D
            }
        """.trimIndent())
    }

    @Test
    fun enumClassCannotInheritInterface() {
        assertSemanticFail("""
            interface SomeInterface
            enum class MyEnum : SomeInterface {
                A, B, C, D
            }
        """.trimIndent())
    }

    @Test
    fun enumClassCannotBeOpen() {
        assertSemanticFail("""
            open enum class MyEnum {
                A, B, C, D
            }
        """.trimIndent())
    }

    @Test
    fun enumClassConstructorMismatch1() {
        assertSemanticFail("""
            enum class MyEnum {
                A, B, C(123), D
            }
        """.trimIndent())
    }

    @Test
    fun enumClassConstructorMismatch2() {
        assertSemanticFail("""
            enum class MyEnum(val value: Int) {
                A(12), B(23), C(123, 4), D(45)
            }
        """.trimIndent())
    }

    @Test
    fun enumClassConstructorMismatch3() {
        assertSemanticFail("""
            enum class MyEnum(val value: Int) {
                A(12), B(23), C("123"), D(45)
            }
        """.trimIndent())
    }

    @Test
    fun enumClassConstructorMismatch4() {
        assertSemanticFail("""
            enum class MyEnum(val value: Int) {
                A(12), B(23), C, D(45)
            }
        """.trimIndent())
    }
}
