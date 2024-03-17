package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.extension.fullClassName

sealed interface ComparableRuntimeValue<T : Comparable<T>> : RuntimeValue, Comparable<ComparableRuntimeValue<T>>

sealed interface ComparableRuntimeValueHolder<T : Comparable<T>> : ComparableRuntimeValue<T>, KotlinValueHolder<T> {
    override val value: T

    override fun compareTo(other: ComparableRuntimeValue<T>): Int {
        if (other !is ComparableRuntimeValueHolder) {
            throw RuntimeException("Compare target is not a ComparableRuntimeValueHolder but ${other::class.fullClassName}")
        }
        return value.compareTo(other.value)
    }
}
