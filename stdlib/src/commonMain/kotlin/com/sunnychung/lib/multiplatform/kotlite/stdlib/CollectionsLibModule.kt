package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.CollectionInterface
import com.sunnychung.lib.multiplatform.kotlite.model.IterableInterface
import com.sunnychung.lib.multiplatform.kotlite.model.ListValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapEntryValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableListValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableMapValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableSetValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.SetValue

class CollectionsLibModule : AbstractCollectionsLibModule() {
    override val classes: List<ProvidedClassDefinition> = listOf(
        // they are now part of the interpreter core
//        IterableInterface.clazz,
//        CollectionInterface.collectionClazz,
//        ListValue.clazz,

        CollectionInterface.mutableCollectionClazz,
        MutableListValue.clazz,
        MapValue.clazz,
        MutableMapValue.clazz,
        MapEntryValue.clazz,
        SetValue.clazz,
        MutableSetValue.clazz,
    )
}
