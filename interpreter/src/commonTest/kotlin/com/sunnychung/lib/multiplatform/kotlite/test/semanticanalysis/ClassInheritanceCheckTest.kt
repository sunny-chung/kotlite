package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class ClassInheritanceCheckTest {

    @Test
    fun cannotExtendFromNonOpenClass() {
        assertSemanticFail("""
            class A
            class B : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassProperty1() {
        assertSemanticFail("""
            open class A {
                var a = 1
                var c = a + b
            }
            class B(var b: Int) : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassProperty2() {
        assertSemanticFail("""
            open class A {
                var a = 1
                var c = a + this.b
            }
            class B(var b: Int) : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassProperty3() {
        assertSemanticFail("""
            open class A {
                var a = 1
                fun sum(): Int = a + b
            }
            class B(var b: Int) : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassProperty4() {
        assertSemanticFail("""
            open class A {
                var a = 1
                fun sum(): Int = a + this.b
            }
            class B(var b: Int) : A()
        """.trimIndent())
    }

    @Test
    fun accessSubclassFunction1() {
        assertSemanticFail("""
            open class A {
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
            open class A {
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
            open class A {
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
            open class A {
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

    @Test
    fun cannotOverrideNonOpenFunction() {
        assertSemanticFail("""
            open class A {
                fun f() {}
            }
            class B : A() {
                override fun f() {}
            }
        """.trimIndent())
    }

    @Test
    fun cannotOverrideFunctionWithoutOverrideModifier() {
        assertSemanticFail("""
            open class A {
                open fun f() {}
            }
            class B : A() {
                fun f() {}
            }
        """.trimIndent())
    }

}