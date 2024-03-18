package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateTimeFormatValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateTimeFormattableInterface
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDurationValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KInstantValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KPointOfTimeValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZoneOffsetValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZonedDateTimeValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZonedInstantValue

class KDateTimeLibModule : AbstractKDateTimeLibModule() {
    override val classes: List<ProvidedClassDefinition> = listOf(
        KDateTimeFormattableInterface.interfaze,
        KPointOfTimeValue.clazz,
        KDateTimeFormatValue.clazz,
        KDateValue.clazz,
        KDurationValue.clazz,
        KInstantValue.clazz,
        KZonedDateTimeValue.clazz,
        KZonedInstantValue.clazz,
        KZoneOffsetValue.clazz,
    )
}
