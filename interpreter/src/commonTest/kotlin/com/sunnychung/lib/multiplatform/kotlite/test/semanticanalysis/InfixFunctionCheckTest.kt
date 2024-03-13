package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import com.sunnychung.lib.multiplatform.kotlite.error.ParseException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class InfixFunctionCheckTest {

    @Test
    fun infixFunctionMustBeMemberFunctionOrExtensionFunction1() {
        assertSemanticFail("""
            infix fun abc(x: Int) = x
        """.trimIndent())
    }

    @Test
    fun infixFunctionMustBeMemberFunctionOrExtensionFunction2() {
        assertSemanticFail("""
            class A {
                fun f(): Int {
                    infix fun abc(x: Int) = x
                    return 10
                }
            }
        """.trimIndent())
    }

    @Test
    fun infixFunctionMustHaveSingleValueParameter() {
        assertSemanticFail("""
            class A
            infix fun A.abc(x: Int, y: Int) = x + y
        """.trimIndent())
    }

    @Test
    fun infixFunctionMustNotHaveVararg() {
        assertSemanticFail("""
            class A
            infix fun A.abc(vararg x: Int) = 1
        """.trimIndent())
    }

    @Test
    fun infixFunctionMustNotHaveParameterWithDefaultValue() {
        assertSemanticFail("""
            class A
            infix fun A.abc(x: Int = 10) = 1
        """.trimIndent())
    }

    @Test
    fun infixFunctionCallMustCallWithReceiver() {
        assertFailsWith<ParseException> { semanticAnalyzer("""
                class A {
                    infix fun abc(x: Int) = 1
                    fun f() {
                        abc 20
                    }
                }
            """.trimIndent())
            .analyze()
        }
    }

    @Test
    fun infixFunctionMustMarkWithInfixModifier() {
        assertSemanticFail("""
            class A
            fun A.abc(x: Int) = x
            val x = A() abc 10
        """.trimIndent())
    }
}
