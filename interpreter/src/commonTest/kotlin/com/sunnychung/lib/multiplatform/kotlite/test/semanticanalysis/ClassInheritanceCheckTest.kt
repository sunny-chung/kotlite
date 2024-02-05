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

    @Test
    fun cannotOverrideInapplicableFunction() {
        assertSemanticFail("""
            open class A {
                open fun f(x: String) {}
            }
            class B : A() {
                override fun f(x: Int) {}
            }
        """.trimIndent())
    }

    @Test
    fun cannotRedeclareProperty1() {
        assertSemanticFail("""
            open class A {
                val x = 1
            }
            class B : A() {
                val x = 2
            }
        """.trimIndent())
    }

    @Test
    fun cannotRedeclareProperty2() {
        assertSemanticFail("""
            open class A(var x: Int)
            class B : A(1) {
                val x = 2
            }
        """.trimIndent())
    }

    @Test
    fun cannotRedeclareProperty3() {
        assertSemanticFail("""
            open class A {
                val x = 1
            }
            class B(var x: Int) : A()
        """.trimIndent())
    }

    @Test
    fun cannotRedeclareProperty4() {
        assertSemanticFail("""
            open class A(var x: Int)
            class B(var x: Int) : A(x)
        """.trimIndent())
    }

    @Test
    fun cannotOverrideNonOpenProperty() {
        assertSemanticFail("""
            open class A {
                val x = 1
            }
            class B : A() {
                override val x = 2
            }
        """.trimIndent())
    }

    @Test
    fun cannotOverridePropertyWithoutOverrideModifier() {
        assertSemanticFail("""
            open class A {
                open var x = 1
            }
            class B : A() {
                var x = 2
            }
        """.trimIndent())
    }

    @Test
    fun cannotOverrideInapplicableProperty() {
        assertSemanticFail("""
            open class A
            class B : A() {
                override var x = 2
            }
        """.trimIndent())
    }

    @Test
    fun simpleSuperClassTypeParameter() {
        assertSemanticFail("""
            open class A<T> {
                var a: T? = null
            }
            class B : A<String>()
            val x = B()
            x.a = 123
        """.trimIndent())
    }

    @Test
    fun nonexistSuperClassTypeParameter() {
        assertSemanticFail("""
            open class A<T> {
                var a: T? = null
            }
            open class B : A<T>()
            open class C<T> : B()
            open class D<T> : C<T>()
            class E<T> : D<T>()
        """.trimIndent())
    }

}