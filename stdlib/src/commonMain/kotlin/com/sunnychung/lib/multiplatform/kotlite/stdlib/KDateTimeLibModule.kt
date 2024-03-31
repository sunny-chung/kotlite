package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateTimeFormatClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateTimeFormattableInterface
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDurationClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KInstantClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KPointOfTimeClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZoneOffsetClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZonedDateTimeClass
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZonedInstantClass

class KDateTimeLibModule : AbstractKDateTimeLibModule() {
    override val classes: List<ProvidedClassDefinition> = listOf(
        KDateTimeFormattableInterface.interfaze,
        KPointOfTimeClass.clazz,
        KDateTimeFormatClass.clazz,
        KDateClass.clazz,
        KDurationClass.clazz,
        KInstantClass.clazz,
        KZonedDateTimeClass.clazz,
        KZonedInstantClass.clazz,
        KZoneOffsetClass.clazz,
    )
}
