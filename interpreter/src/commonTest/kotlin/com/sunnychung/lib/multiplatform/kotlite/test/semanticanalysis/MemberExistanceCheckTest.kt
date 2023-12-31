package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Ignore
import kotlin.test.Test

class MemberExistanceCheckTest {

    @Test
    fun nonexistMemberProperty() {
        assertSemanticFail("""
            class Cls {
                val a: Int = 10
            }
            val o: Cls = Cls()
            o.b = 20
        """.trimIndent())
    }

    @Test
    fun nonexistMemberFunction() {
        assertSemanticFail("""
            class Cls {
                val a: Int = 10
                fun f() = a
            }
            Cls().g()
        """.trimIndent())
    }

    @Test
    @Ignore // not yet implement
    fun nonexistMemberFunctionAfterInterCall1() {
        assertSemanticFail("""
            class A {
                val a: Int = 10
                val o: B? = null
                fun f() {
                    o?.g()
                }
            }
            class B {
                val a: Int = 10
                val o: A? = null
                fun f() {
                    o?.h()
                }
            }
            val a: A = A()
            val b: B = B()
            a.o = B
            b.o = A
            a.f()
        """.trimIndent())
    }

    @Test
    @Ignore // not yet implement
    fun nonexistMemberFunctionAfterInterCall2() {
        assertSemanticFail("""
            class A {
                val a: Int = 10
                val o: B? = null
                fun f() {
                    o?.g()
                }
            }
            class B {
                val a: Int = 10
                val o: A? = null
                fun g() {
                    o?.h()
                }
            }
            val a: A = A()
            val b: B = B()
            a.o = B
            b.o = A
            a.b.a.f()
        """.trimIndent())
    }
}
