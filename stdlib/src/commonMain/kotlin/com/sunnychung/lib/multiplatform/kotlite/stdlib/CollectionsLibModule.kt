package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.ListValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableListValue

class CollectionsLibModule : AbstractCollectionsLibModule() {
    override val classes: List<ProvidedClassDefinition> = listOf(ListValue.clazz, MutableListValue.clazz)
}
