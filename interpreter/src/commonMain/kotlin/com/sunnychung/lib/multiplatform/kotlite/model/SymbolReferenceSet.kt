package com.sunnychung.lib.multiplatform.kotlite.model

class SymbolReferenceSet(val scopeLevel: Int) {
    val properties: MutableSet<String> = mutableSetOf()
    val functions: MutableSet<String> = mutableSetOf()
    val classes: MutableSet<String> = mutableSetOf()
    val typeAlias: MutableSet<String> = mutableSetOf()
}
