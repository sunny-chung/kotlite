package com.sunnychung.lib.multiplatform.kotlite.model

abstract class LibraryModule(val name: String) {
    abstract val functions: List<CustomFunctionDefinition>
}
