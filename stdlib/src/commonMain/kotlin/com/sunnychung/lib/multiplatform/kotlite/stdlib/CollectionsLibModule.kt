package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.CollectionInterface
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapEntryClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableListClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableMapClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableSetClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.SetClass

class CollectionsLibModule : AbstractCollectionsLibModule() {
    override val classes: List<ProvidedClassDefinition> = listOf(
        // they are now part of the interpreter core
//        IterableInterface.clazz,
//        CollectionInterface.collectionClazz,
//        ListValue.clazz,

        CollectionInterface.mutableCollectionClazz,
        MutableListClass.clazz,
        MapClass.clazz,
        MutableMapClass.clazz,
        MapEntryClass.clazz,
        SetClass.clazz,
        MutableSetClass.clazz,
    )
}
