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
    fun nonexistMemberFunctionOnNestedCall() {
        assertSemanticFail("""
            class B {
                val a: Int = 10
                fun f() {
                    
                }
            }
            class A {
                val a: Int = 10
                val o: B? = null
                fun f() {
                    o?.g()
                }
            }
            val a: A = A()
            val b: B = B()
            a.o = b
            a.f()
        """.trimIndent())
    }

    @Test
    fun nonexistMemberFunctionOnNestedPath() {
        assertSemanticFail("""
            class B {
                val a: Int = 10
                fun f() {
                    h()
                }
            }
            class A {
                val a: Int = 10
                val o: B? = null
                fun f() {
                    o?.f()
                }
            }
            val a: A = A()
            val b: B = B()
            a.o = b
            a.o.f()
        """.trimIndent())
    }

    @Test
    fun nonexistMemberPropertyOnNestedCall() {
        assertSemanticFail("""
            class B {
                val a: Int = 10
                fun f() {
                    
                }
            }
            class A {
                val a: Int = 10
                val o: B? = null
                fun f() {
                    o?.z = 1
                }
            }
            val a: A = A()
            val b: B = B()
            a.o = b
            a.f()
        """.trimIndent())
    }

    @Test
    fun nonexistMemberPropertyOnNestedPath() {
        assertSemanticFail("""
            class B {
                val a: Int = 10
                fun f() {
                    
                }
            }
            class A {
                val a: Int = 10
                val o: B? = null
                fun f() {
                    o?.f()
                }
            }
            val a: A = A()
            val b: B = B()
            a.o = b
            a.o.z = 1
        """.trimIndent())
    }

    @Test
    @Ignore // not support cyclic class dependency
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
            a.o = b
            b.o = a
            a.f()
        """.trimIndent())
    }

    @Test
    @Ignore // not support cyclic class dependency
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
            a.o = b
            b.o = a
            a.b.a.f()
        """.trimIndent())
    }
}
