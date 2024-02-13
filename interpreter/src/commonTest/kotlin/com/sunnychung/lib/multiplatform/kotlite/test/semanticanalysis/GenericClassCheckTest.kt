package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Ignore
import kotlin.test.Test

class GenericClassCheckTest {

    @Test
    fun missingGenericArgument() {
        assertSemanticFail("""
            class MyVal1<T>(val value: T) {
                fun getValue(): T = value
            }
            class MyVal2<T>(val value: T) {
                fun getValue(): T = value
            }
            val o = MyVal1<MyVal2>(MyVal2(null))
        """.trimIndent())
    }

    @Test
    fun incorrectGenericArgument2() {
        assertSemanticFail("""
            class MyVal1<T>(val value: T) {
                fun getValue(): T = value
            }
            class MyVal2<T>(val value: T) {
                fun getValue(): T = value
            }
            val o = MyVal1<MyVal2<Int>>(10)
        """.trimIndent())
    }

    @Test
    fun incompatibleConstantInGenericMemberFunction1() {
        assertSemanticFail("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
                fun f(): T = 10
            }
        """.trimIndent())
    }

    @Test
    fun incompatibleConstantInGenericMemberFunction2() {
        assertSemanticFail("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
                fun f(): T = 10 as Any?
            }
        """.trimIndent())
    }

    @Test
    fun incorrectAssignment() {
        assertSemanticFail("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
            }
            val o = MyVal1<Int>(10)
            o.value = "abc"
        """.trimIndent())
    }

    @Test
    fun impossibleCastOutsideGenerics() {
        assertSemanticFail("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
            }
            val o = MyVal1<Int>(10)
            o.value as T
        """.trimIndent())
    }

    @Test
    fun impossibleUseOfTypeParameterOutsideGenerics() {
        assertSemanticFail("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
            }
            val o = MyVal1<Int>(10)
            val a: T = o.value
        """.trimIndent())
    }

    @Test
    fun incompatibleGenericLambdaReturnType() {
        assertSemanticFail("""
            class MyVal2<T>(val value: T) {
                fun getValueReader(): () -> T = { value }
            }
            val o = MyVal2<Int>(10)
            val a: Double = o.getValueReader()()
        """.trimIndent())
    }

    @Test
    fun incorrectLambdaArgument() {
        assertSemanticFail("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
                fun func(op: () -> T): T = op()
            }
            val o = MyVal1<Int>(10)
            o.func { "abc" }
        """.trimIndent())
    }

    @Test
    fun incorrectLambdaArgument2() {
        assertSemanticFail("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
                fun func(op: () -> T) = op()
            }
            val o = MyVal1<Int>(10)
            o.func { "abc" }
        """.trimIndent())
    }

    @Test
    fun incorrectNestedTypeArgument1() {
        assertSemanticFail("""
            class MyPair<A, B>(val first: A, val second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first
                fun getSecond(): B {
                    return second
                }
            }
            class MyVal<T>(val value: T) {
                fun getValue(): T = value
            }
            val p = MyPair<MyVal<MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>>, Double>(
                MyVal<MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>>(
                    MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>(
                        MyVal<MyVal<MyVal<MyPair<Int, String>>>>(
                                MyVal<MyPair<Int, String>>(
                                    MyPair<Int, String>(10, "abc")
                                )
                        )
                    )
                ),
                2.345
            )
        """.trimIndent())
    }

    @Test
    fun incorrectNestedTypeArgument2() {
        assertSemanticFail("""
            class MyPair<A, B>(val first: A, val second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first
                fun getSecond(): B {
                    return second
                }
            }
            class MyVal<T>(val value: T) {
                fun getValue(): T = value
            }
            val p = MyPair<MyVal<MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>>, Double>(
                MyVal<MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>>(
                    MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>(
                        MyVal<MyVal<MyVal<MyPair<Int, String>>>>(
                            MyVal<MyVal<MyPair<Int, String>>>(
                                MyVal<MyPair<Int, String>>(
                                    MyPair<Int, String>(10, 1.23)
                                )
                            )
                        )
                    )
                ),
                2.345
            )
        """.trimIndent())
    }

    @Test
    fun incorrectNestedTypeArgument3() {
        assertSemanticFail("""
            class MyPair<A, B>(val first: A, val second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first
                fun getSecond(): B {
                    return second
                }
            }
            class MyVal<T>(val value: T) {
                fun getValue(): T = value
            }
            val p = MyPair<MyVal<MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>>, Double>(
                MyVal<MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>>(
                    MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>(
                        MyVal<MyVal<MyVal<MyPair<Int, String>>>>(
                            MyVal<MyVal<MyPair<Int, String>>>(
                                MyVal<MyPair<Int, String>>(
                                    MyPair<Int, Double>(10, "abc")
                                )
                            )
                        )
                    )
                ),
                2.345
            )
        """.trimIndent())
    }

    @Test
    fun incorrectReturnTypeInLambda() {
        assertSemanticFail("""
            class MyVal<T>(val value: T) {
                fun getValueReader(): () -> T = { 10 }
            }
        """.trimIndent())
    }

    @Test
    fun incorrectTypeParameterConversionInLambdaBody() {
        assertSemanticFail("""
            class MyVal<T>(val value: T) {
                fun getValueReader(): () -> T = {
                    val x: T = 10
                    value
                }
            }
        """.trimIndent())
    }

    @Test
    fun incorrectReturnTypeInNestedLambda() {
        assertSemanticFail("""
            class MyVal<T>(val value: T) {
                fun getValueReader(): () -> (() -> T) = { { 10 } }
            }
        """.trimIndent())
    }

    @Test
    fun incorrectTypeParameterConversionInNestedLambdaBody() {
        assertSemanticFail("""
            class MyVal<T>(val value: T) {
                fun getValueReader(): () -> (() -> T) = { {
                    val x: T = 10
                    value
                } }
            }
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenOtherClassNameIsSameAsTypeParameterName1() {
        assertSemanticFail("""
            class T {
                fun f(): Int = 5
            }
            class MyVal1<T>(var value: T) {
                fun func(): T = 10
            }
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenOtherClassNameIsSameAsTypeParameterName2() {
        assertSemanticFail("""
            class T {
                fun f(): Int = 5
            }
            class MyVal1<T>(var value: T) {
                fun func(): () -> T = { 10 }
            }
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenOtherClassNameIsSameAsTypeParameterName3() {
        assertSemanticFail("""
            class T {
                fun f(): Int = 5
            }
            class MyVal1<T>(var value: T) {
                fun func(): T = 10 as Any?
            }
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenOtherClassNameIsSameAsTypeParameterName4() {
        assertSemanticFail("""
            class T {
                fun f(): Int = 5
            }
            class MyVal1<T>(var value: T)
            val o = MyVal1<Int>(10)
            o.value = "abc"
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenOtherClassNameIsSameAsTypeParameterName5() {
        assertSemanticFail("""
            class T {
                fun f(): Int = 5
            }
            class MyVal1<T>(var value: T) {
                fun func(op: () -> T): T = op()
            }
            val o = MyVal1<Int>(10)
            o.func { "abc" }
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenOtherClassNameIsSameAsTypeParameterName6() {
        assertSemanticFail("""
            class T {
                fun f(): Int = 5
            }
            class MyVal1<T>(var value: T) {
                fun func(op: () -> T): T = op()
            }
            val o = MyVal1<Int>(10)
            o.func { T() }
        """.trimIndent())
    }

    @Test
    fun castToOtherClassNameWhichIsSameAsTypeParameterNameSucceeds() {
        assertSemanticSuccess("""
            class T {
                fun f(): Int = 5
            }
            class MyVal1<T>(var value: T) {
                fun func(op: () -> T): T = op()
            }
            val o = MyVal1<Int>(10)
            o.value as T // will yield runtime error
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenClassNameIsSameAsTypeParameterName1() {
        assertSemanticFail("""
            class T<T>(var value: T) {
                fun func(): T = 10
            }
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenClassNameIsSameAsTypeParameterName2() {
        assertSemanticFail("""
            class T<T>(var value: T) {
                fun func(): () -> T = { 10 }
            }
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenClassNameIsSameAsTypeParameterName3() {
        assertSemanticFail("""
            class T<T>(var value: T) {
                fun func(): T = 10 as Any?
            }
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenClassNameIsSameAsTypeParameterName4() {
        assertSemanticFail("""
            class T<T>(var value: T)
            val o = T<Int>(10)
            o.value = "abc"
        """.trimIndent())
    }

    @Test
    fun typeErrorWhenClassNameIsSameAsTypeParameterName5() {
        assertSemanticFail("""
            class T<T>(var value: T) {
                fun func(op: () -> T): T = op()
            }
            val o = T<Int>(10)
            o.func { "abc" }
        """.trimIndent())
    }

    @Test
    @Ignore // not implement
    fun typeErrorWhenClassNameIsSameAsTypeParameterName6() {
        assertSemanticFail("""
            class T<T>(var value: T) {
                fun func(op: () -> T): T = op()
            }
            val o = T<Int>(10)
            o.func { T<Int>(20) }
        """.trimIndent())
    }

    @Test
    fun castToClassNameWhichIsSameAsTypeParameterNameSucceeds() {
        assertSemanticSuccess("""
            class T<T>(var value: T) {
                fun func(op: () -> T): T = op()
            }
            val o = T<Int>(10)
            o.value as T<String> // will yield runtime error
        """.trimIndent())
    }

    @Test
    fun invalidPropertyAccessWhenClassNameIsSameAsTypeParameterNameNested() {
        assertSemanticFail("""
            class T<T>(var value: T) {
                fun getValue(): T = value
                fun func(op: () -> T): T = op()
                fun asT(): T = 20 as T
            }
            val o = T<T<T<Int>>>(T<T<Int>>(T<Int>(10)))
            val b = o.value.value.value.value
        """.trimIndent())
    }

    @Test
    fun genericClassAgainstIsOperatorWithoutDiamond() {
        assertSemanticFail("""
            class A<T>
            val b = A<Int>() is A
        """.trimIndent())
    }

    @Test
    fun genericClassAgainstIsOperatorWithIncorrectNumArguments1() {
        assertSemanticFail("""
            class A<T>
            val b = A<Int>() is A<*, *>
        """.trimIndent())
    }

    @Test
    fun genericClassAgainstIsOperatorWithIncorrectNumArguments2() {
        assertSemanticFail("""
            class A<T1, T2>
            val b = A<Int, String>() is A<*>
        """.trimIndent())
    }
}
