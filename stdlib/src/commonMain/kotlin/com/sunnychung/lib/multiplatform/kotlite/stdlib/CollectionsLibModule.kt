package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.CollectionInterface
import com.sunnychung.lib.multiplatform.kotlite.model.IterableInterface
import com.sunnychung.lib.multiplatform.kotlite.model.ListValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapEntryValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableListValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableMapValue

class CollectionsLibModule : AbstractCollectionsLibModule() {
    override val classes: List<ProvidedClassDefinition> = listOf(
        // they are now part of the interpreter core
//        IterableInterface.clazz,
//        CollectionInterface.collectionClazz,
//        ListValue.clazz,

        MutableListValue.clazz,
        MapValue.clazz,
        MutableMapValue.clazz,
        MapEntryValue.clazz,
    )
}
