package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue

fun <T : RuntimeValue> T.toNullable(): T? {
    return if (this === NullValue) {
        null
    } else {
        this
    }
}

fun RuntimeValue?.toRuntimeValue() : RuntimeValue {
    return this ?: NullValue
}

data class BasicMapEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>
fun Map.Entry<RuntimeValue?, RuntimeValue?>.toRuntimeValue() : Map.Entry<RuntimeValue, RuntimeValue> {
    return BasicMapEntry(key.toRuntimeValue(), value.toRuntimeValue())
}

fun RuntimeValue?.toNullValueOr(nonNullValue: (RuntimeValue) -> RuntimeValue) : RuntimeValue {
    return this?.let(nonNullValue) ?: NullValue
}

fun <C : Collection<RuntimeValue?>> C.toNonNullable() : Collection<RuntimeValue> {
    val items = map { it.toRuntimeValue() }
    return when (this) {
        is MutableList<*> -> items.toMutableList()
        is MutableSet<*> -> items.toMutableSet()
        is List<*> -> items
        is Set<*> -> items.toSet()
        else -> throw UnsupportedOperationException()
    }
}
