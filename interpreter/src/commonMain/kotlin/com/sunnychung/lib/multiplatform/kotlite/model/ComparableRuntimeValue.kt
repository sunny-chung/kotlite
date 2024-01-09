package com.sunnychung.lib.multiplatform.kotlite.model

sealed interface ComparableRuntimeValue<T : Comparable<T>> : RuntimeValue, Comparable<ComparableRuntimeValue<T>> {
    val value: T

    override fun compareTo(other: ComparableRuntimeValue<T>): Int {
        return value.compareTo(other.value)
    }
}
