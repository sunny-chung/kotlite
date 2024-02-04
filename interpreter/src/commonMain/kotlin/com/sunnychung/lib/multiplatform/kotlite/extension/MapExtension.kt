package com.sunnychung.lib.multiplatform.kotlite.extension

import com.sunnychung.lib.multiplatform.kotlite.error.DuplicateKeyException

infix fun <K, V> Map<K, V>.merge(another: Map<K, V>): Map<K, V> {
    val result = mutableMapOf<K, V>()
    this.toMap(result)
    another.forEach {
        if (result.containsKey(it.key)) {
            throw DuplicateKeyException("Duplicate key while merging maps")
        }
        result[it.key] = it.value
    }
    return result
}
