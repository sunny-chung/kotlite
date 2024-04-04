package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.stdlib.range.ClosedRangeClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.range.IntProgressionClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.range.IntRangeClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.range.LongProgressionClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.range.LongRangeClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.range.OpenEndRangeClass

class RangeLibModule : AbstractRangeLibModule() {
    override val classes: List<ProvidedClassDefinition> = super.classes +
        listOf(
            ClosedRangeClass.clazz,
            OpenEndRangeClass.clazz,
            IntProgressionClass.clazz,
            IntRangeClass.clazz,
            LongProgressionClass.clazz,
            LongRangeClass.clazz,
        )
}
