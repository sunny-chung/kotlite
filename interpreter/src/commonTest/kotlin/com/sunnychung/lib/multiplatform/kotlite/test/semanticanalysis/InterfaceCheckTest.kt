package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import com.sunnychung.lib.multiplatform.kotlite.error.ParseException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class InterfaceCheckTest {
    @Test
    fun notSupportAbstractProperty1() {
        assertSemanticFail("""
            interface A {
                val a: Int
            }
        """.trimIndent())
    }

    @Test
    fun notSupportAbstractProperty2() {
        assertSemanticFail("""
            interface A {
                var a: Int?
            }
        """.trimIndent())
    }

    @Test
    fun interfaceCannotHaveConcreteProperty1() {
        assertSemanticFail("""
            interface A {
                val a: Int = 0
            }
        """.trimIndent())
    }

    @Test
    fun interfaceCannotHaveConcreteProperty2() {
        assertSemanticFail("""
            interface A {
                var a: Int? = null
            }
        """.trimIndent())
    }

    @Test
    fun notSupportConcreteFunction1() {
        assertFailsWith<ParseException> {
            semanticAnalyzer("""
                interface A {
                    fun f() { }
                }
            """.trimIndent()).analyze()
        }
    }

    @Test
    fun notSupportConcreteFunction2() {
        assertFailsWith<ParseException> {
            semanticAnalyzer("""
                interface A {
                    fun f(): Int = 1
                }
            """.trimIndent()).analyze()
        }
    }

    @Test
    fun interfaceCannotInheritClass() {
        assertSemanticFail("""
            open class B
            interface A : B()
        """.trimIndent())
    }

    @Test
    fun interfaceCannotBeConstructed() {
        assertSemanticFail("""
            interface A
            val x = A()
        """.trimIndent())
    }

    @Test
    fun interfaceCannotBeConstructedInSuperDeclaration() {
        assertSemanticFail("""
            interface A
            class B : A()
        """.trimIndent())
    }

    @Test
    fun notSupportNestedInterface() {
        assertSemanticFail("""
            interface A {
                interface B
            }
        """.trimIndent())
    }

    @Test
    fun notSupportNestedClass() {
        assertSemanticFail("""
            interface A {
                class B
            }
        """.trimIndent())
    }

    @Test
    fun classDoesNotImplementInterfaceFunction() {
        assertSemanticFail("""
            interface I {
                fun a()
            }
            class A: I
        """.trimIndent())
    }

    @Test
    fun classDoesNotImplementInheritedInterfaceFunction() {
        assertSemanticFail("""
            interface I1 {
                fun a()
            }
            interface I2 : I1
            class A: I2
        """.trimIndent())
    }

    @Test
    fun interfaceFunctionClash() {
        assertSemanticFail("""
            interface I1 {
                fun a()
            }
            interface I2 {
                fun a()
            }
            class A: I1, I2
        """.trimIndent())
    }
}
