package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class ClassInheritanceCheckTest {

    @Test
    fun accessSubclassProperty1() {
        assertSemanticFail("""
            class A {
                var a = 1
                var c = a + b
            }
            class B(var b: Int) : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassProperty2() {
        assertSemanticFail("""
            class A {
                var a = 1
                var c = a + this.b
            }
            class B(var b: Int) : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassProperty3() {
        assertSemanticFail("""
            class A {
                var a = 1
                fun sum(): Int = a + b
            }
            class B(var b: Int) : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassProperty4() {
        assertSemanticFail("""
            class A {
                var a = 1
                fun sum(): Int = a + this.b
            }
            class B(var b: Int) : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassFunction1() {
        assertSemanticFail("""
            class A {
                var a = 1
                val c = sub()
            }
            class B(var b: Int) : A() {
                fun sub() {}
            }
        """.trimIndent())
    }

    @Test
    fun accessSubclassFunction2() {
        assertSemanticFail("""
            class A {
                var a = 1
                val c = this.sub()
            }
            class B(var b: Int) : A() {
                fun sub() {}
            }
        """.trimIndent())
    }

    @Test
    fun accessSubclassFunction3() {
        assertSemanticFail("""
            class A {
                var a = 1
                fun f() {
                    sub()
                }
            }
            class B(var b: Int) : A() {
                fun sub() {}
            }
        """.trimIndent())
    }

    @Test
    fun accessSubclassFunction4() {
        assertSemanticFail("""
            class A {
                var a = 1
                fun f() {
                    this.sub()
                }
            }
            class B(var b: Int) : A() {
                fun sub() {}
            }
        """.trimIndent())
    }

}