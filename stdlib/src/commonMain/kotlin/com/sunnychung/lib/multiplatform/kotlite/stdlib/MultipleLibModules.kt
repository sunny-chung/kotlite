package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.GlobalProperty
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition

/**
 * @param modules An ordered list of modules where the one depends on another one should be placed later.
 */
open class MultipleLibModules(name: String, val modules: List<LibraryModule>) : LibraryModule(name) {
    override val classes: List<ProvidedClassDefinition> = modules.flatMap { it.classes }
    override val properties: List<ExtensionProperty> = modules.flatMap { it.properties }
    override val globalProperties: List<GlobalProperty> = modules.flatMap { it.globalProperties }
    override val functions: List<CustomFunctionDefinition> = modules.flatMap { it.functions }
}
