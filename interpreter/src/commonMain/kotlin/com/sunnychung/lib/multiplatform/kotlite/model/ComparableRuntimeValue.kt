package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.extension.fullClassName

sealed interface ComparableRuntimeValue<T, C : Any> : RuntimeValue, Comparable<ComparableRuntimeValue<T, C>>

sealed interface ComparableRuntimeValueHolder<T, C : Any> : ComparableRuntimeValue<T, C>, KotlinValueHolder<T> {
    override val value: T

    override fun compareTo(other: ComparableRuntimeValue<T, C>): Int {
        if (other !is ComparableRuntimeValueHolder<T, C>) {
            throw RuntimeException("Compare target is not a ComparableRuntimeValueHolder but ${other::class.fullClassName}")
        }
        return (value as Comparable<C>).compareTo(other.value as C)
    }
}
