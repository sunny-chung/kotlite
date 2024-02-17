package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionModifier
import com.sunnychung.lib.multiplatform.kotlite.model.ListValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.PairValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableListValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableMapValue

class CollectionsLibModule : AbstractCollectionsLibModule() {
    override val classes: List<ProvidedClassDefinition> = listOf(
        ListValue.clazz,
        MutableListValue.clazz,
        MapValue.clazz,
        MutableMapValue.clazz,
    )
}
