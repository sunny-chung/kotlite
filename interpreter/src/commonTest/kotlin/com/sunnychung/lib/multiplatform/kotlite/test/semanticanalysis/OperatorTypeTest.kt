package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import kotlin.test.Test

class OperatorTypeTest {

    @Test
    fun wrongGetterType() {
        assertSemanticFail("""
            class MyPair<T>(var first: T, var second: T) {
                operator fun get(index: Int): T {
                    return if (index == 0) {
                        first
                    } else if (index == 1) {
                        second
                    } else {
                        first // TODO: throw exception
                    }
                }
            }
            val y = MyPair("ab", "cde")
            var a = 10
            a = y[0]
        """.trimIndent())
    }

    @Test
    fun wrongSetterType() {
        assertSemanticFail("""
            class MyPair<T>(var first: T, var second: T) {
                operator fun get(index: Int): T {
                    return if (index == 0) {
                        first
                    } else if (index == 1) {
                        second
                    } else {
                        first // TODO: throw exception
                    }
                }
                operator fun set(index: Int, newValue: T) {
                    ++numSetCalls
                    if (index == 0) {
                        first = newValue
                    } else {
                        second = newValue
                    }
                }
            }
            val y = MyPair("ab", "cde")
            y[0] = 123
        """.trimIndent())
    }

    @Test
    fun wrongPlusAssignType() {
        assertSemanticFail("""
            class MyPair<T>(var first: T, var second: T) {
                operator fun get(index: Int): T {
                    return if (index == 0) {
                        first
                    } else if (index == 1) {
                        second
                    } else {
                        first // TODO: throw exception
                    }
                }
                operator fun set(index: Int, newValue: T) {
                    ++numSetCalls
                    if (index == 0) {
                        first = newValue
                    } else {
                        second = newValue
                    }
                }
            }
            val y = MyPair(12, 34)
            y[0] += 5.67
        """.trimIndent())
    }

    @Test
    fun plusFunctionWithoutOperatorModifier() {
        assertSemanticFail("""
            class A
            class B
            fun A.plus(x: B): B = B()
            var a = A() + B()
        """.trimIndent())
    }

    @Test
    fun augmentedAssignmentWrongPreAssignmentOperatorType() {
        assertSemanticFail("""
            class A
            class B
            operator fun A.plus(x: B): B = B()
            var a = A()
            a += B()
        """.trimIndent())
    }

    @Test
    fun ambiguousAugmentedAssignment() {
        assertSemanticFail("""
            class A
            class B
            operator fun A.plus(x: B): A = A()
            operator fun A.plusAssign(x: B) {}
            var a = A()
            a += B()
        """.trimIndent())
    }

    @Test
    fun compareToShouldContainSingleParameter1() {
        assertSemanticFail("""
            class A {
                operator fun compareTo(): Int = 1
            }
        """.trimIndent())
    }

    @Test
    fun compareToShouldContainSingleParameter2() {
        assertSemanticFail("""
            class A {
                operator fun compareTo(a: A, b: A): Int = 1
            }
        """.trimIndent())
    }

    @Test
    fun compareToShouldReturnInt1() {
        assertSemanticFail("""
            class A {
                operator fun compareTo(a: A): Double = 1.2
            }
        """.trimIndent())
    }

    @Test
    fun compareToShouldReturnInt2() {
        assertSemanticFail("""
            class A {
                operator fun compareTo(a: A): Int? = 1
            }
        """.trimIndent())
    }

    @Test
    fun containsShouldReturnBoolean1() {
        assertSemanticFail("""
            class A {
                operator fun contains(a: Int): Int = 1
            }
        """.trimIndent())
    }

    @Test
    fun containsShouldReturnBoolean2() {
        assertSemanticFail("""
            class A {
                operator fun contains(a: Int): Boolean? {
                    return true
                }
            }
        """.trimIndent())
    }

    @Test
    fun nonComparableClass() {
        assertSemanticFail("""
            class A
            val b = A() > 10
        """.trimIndent())
    }

    @Test
    fun nonAdditiveClass() {
        assertSemanticFail("""
            class A
            val b = A() + A()
        """.trimIndent())
    }

    @Test
    fun compareByteEqualityWithOtherType1() {
        assertSemanticFail("""
            val x: Byte = 10
            val y: Int = 10
            val b = x == y
        """.trimIndent())
    }

    @Test
    fun compareByteEqualityWithOtherType2() {
        assertSemanticFail("""
            val x: Byte = 10
            val y: Int = 10
            val b = y == x
        """.trimIndent())
    }
}
