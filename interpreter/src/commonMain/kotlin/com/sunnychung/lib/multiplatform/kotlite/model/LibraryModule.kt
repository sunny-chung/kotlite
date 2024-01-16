package com.sunnychung.lib.multiplatform.kotlite.model

abstract class LibraryModule(val name: String) {
    abstract val classes: List<ProvidedClassDefinition>
    abstract val properties: List<ExtensionProperty>
    abstract val functions: List<CustomFunctionDefinition>
}
