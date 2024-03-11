package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class PreliminaryTypeCheckTest {
    @Test
    fun assignValue() {
        assertTypeCheckFail("""
            val a: Double = 10.0
            var b: Int = 2
            b = a
        """.trimIndent())
    }

    @Test
    fun assignValueNullableSuccess() {
        semanticAnalyzer("""
            var a: Double? = 10.0
            a = null
        """.trimIndent()).analyze()
    }

    @Test
    fun assignValueNullable1() {
        assertTypeCheckFail("""
            var a: Double = 10.0
            a = null
        """.trimIndent())
    }

    @Test
    fun assignValueNullable2() {
        assertTypeCheckFail("""
            var a: Double = 10.0
            a += null
        """.trimIndent())
    }

    @Test
    fun assignValueNullable3() {
        assertSemanticFail("""
            var a: Double? = 10.0
            a += null
        """.trimIndent())
    }

    @Test
    fun declareInitialValue() {
        assertTypeCheckFail("""
            val a: Double = 10.0
            val b: Int = a
        """.trimIndent())
    }

    @Test
    fun returnType() {
        assertTypeCheckFail("""
            fun f(): Double {
                return 10
            }
        """.trimIndent())
    }

    @Test
    fun returnTypeNested() {
        assertTypeCheckFail("""
            fun f(): Double {
                while (true) {
                    if (true) {
                        return 10
                    }
                }
                return 10.0
            }
        """.trimIndent())
    }

    @Test
    fun returnTypeNullableSuccess1() {
        semanticAnalyzer("""
            fun f(): Double? {
                return 10.0
            }
        """.trimIndent()).analyze()
    }

    @Test
    fun returnTypeNullableSuccess2() {
        semanticAnalyzer("""
            fun f(): Double? {
                return null
            }
        """.trimIndent()).analyze()
    }

    @Test
    fun returnTypeNullableSuccess3() {
        semanticAnalyzer("""
            fun f(): Double? {
                if (true) {
                    return null
                } else {
                    return 20.0
                }
                return 10.0
            }
        """.trimIndent()).analyze()
    }

    @Test
    fun assignFunctionValue() {
        assertTypeCheckFail("""
            fun f(): Double {
                return 10.0
            }
            val x: Int = f()
        """.trimIndent())
    }

    @Test
    fun operator() {
        assertSemanticFail("""
            fun f(): Double {
                return 10.0
            }
            f() + true
        """.trimIndent())
    }

    @Test
    fun ifExpressionSameType() {
        assertSemanticFail("""
            val x: Int = if (true) {
                10.0
            } else {
                20.0
            }
        """.trimIndent())
    }

    @Test
    fun ifExpressionDifferentType() {
        assertSemanticFail("""
            val x: Int = if (true) {
                10.0
            } else {
                20
            }
        """.trimIndent())
    }

    @Test
    fun ifExpressionDifferentNullable() {
        assertSemanticFail("""
            val x: Int = if (true) {
                10
            } else {
                null
            }
        """.trimIndent())
    }

    @Test
    fun ifExpressionNullableSuccess() {
        semanticAnalyzer("""
            val x: Int? = if (false) {
                10
            } else {
                null
            }
        """.trimIndent()).analyze()
    }
}
