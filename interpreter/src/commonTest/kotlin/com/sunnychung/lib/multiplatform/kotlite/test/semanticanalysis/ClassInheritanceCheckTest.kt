package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFails

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

    @Test
    fun nonexistTypeParameterInSuperClass() {
        assertSemanticFail("""
            open class A<T> {
                var a: T2? = null
            }
            class B<T1, T2> : A<T2>()
        """.trimIndent())
    }

    @Test
    fun cannotFormCyclicClassHierarchy1() {
        assertFails {
            semanticAnalyzer("""
                open class A : A()
            """.trimIndent()).analyze()
        }
    }

    @Test
    fun cannotFormCyclicClassHierarchy2() {
        assertFails {
            semanticAnalyzer("""
                open class A : C()
                open class B : A()
                open class C : B()
            """.trimIndent()).analyze()
        }
    }

    @Test
    fun cannotOverrideGenericFunctionWithoutOverrideModifier() {
        assertSemanticFail("""
            open class A<T> {
                open fun f(x: T) {}
            }
            class B<T1, T2> : A<T2>() {
                open fun f(x: T1) {}
            }
        """.trimIndent())
    }

    @Test
    fun superCannotBeAccessedWhenThereIsNoParent1() {
        assertSemanticFail("""
            open class A {
                open fun f() {
                    super.f()
                }
            }
        """.trimIndent())
    }

    @Test
    fun superCannotBeAccessedWhenThereIsNoParent2() {
        assertSemanticFail("""
            open class A {
                open fun f() {
                    super.f()
                }
            }
            class B : A() {
                override fun f() {
                    super.f()
                }
            }
        """.trimIndent())
    }

    @Test
    fun accessNonExistSuperProperty() {
        assertSemanticFail("""
            open class A
            open class B : A() {
                open var a: Int = 1
                fun f() {
                    super.a
                }
            }
            open class C : B() {
                override var a: Int = 2
            }
            class D : C() {
                override var a: Int = 3
            }
        """.trimIndent())
    }

    @Test
    fun assignNonEqualGenericType() {
        assertSemanticFail("""
            open class A
            class B : A()

            class G<T>

            val a: G<A> = G<B>()
        """.trimIndent())
    }

    @Test
    fun overrideFunctionChangeReturnType() {
        assertSemanticFail("""
            open class A {
                open fun f(): Int = 10
            }
            class B: A() {
                override fun f(): String = "abc"
            }
        """.trimIndent())
    }

    @Test
    fun overrideFunctionChangeGenericReturnType() {
        assertSemanticFail("""
            open class A<T> {
                open fun f(): T? = null
            }
            class B: A<String>() {
                override fun f(): Any? = null
            }
        """.trimIndent())
    }

    @Test
    fun overrideFunctionAllowedChangeGenericReturnType() {
        assertSemanticSuccess("""
            open class A<T> {
                open fun f(): T? = null
            }
            class B: A<Any>() {
                override fun f(): Any? = null
            }
        """.trimIndent())
    }

    @Test
    fun overrideGenericFunctionChangeReturnType1() {
        assertSemanticFail("""
            open class A<T> {
                open fun f(a: T, b: Int): T = a
            }
            class B<T>: A<T>() {
                override fun f(a: T, b: Int): String = "abc"
            }
        """.trimIndent())
    }

    @Test
    fun overrideGenericFunctionChangeReturnType2() {
        assertSemanticFail("""
            open class A<T> {
                open fun f(a: T, b: Int): T = a
            }
            class B: A<Int>() {
                override fun f(a: Int, b: Int): String = "abc"
            }
        """.trimIndent())
    }

    @Test
    @Ignore // TODO fix this
    fun overrideGenericFunctionAllowedChangeReturnType() {
        assertSemanticSuccess("""
            open class A<T> {
                open fun f(a: T, b: Int): T = a
            }
            class B: A<Int>() {
                override fun f(a: Int, b: Int): Int = 10
            }
        """.trimIndent())
    }
}