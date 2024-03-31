package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition

object KDateTimeFormattableInterface {
    val interfaze = ProvidedClassDefinition(
        fullQualifiedName = "KDateTimeFormattable",
        isInterface = true,
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition("KDateTime", 1, 1),
    )
}
