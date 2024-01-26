package com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime

import com.sunnychung.lib.multiplatform.kdatetime.KDuration
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition

class KDurationValue(value: KDuration) : DelegatedValue<KDuration>(value, clazz) {
    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "KDuration",
            typeParameters = emptyList(),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { interpreter, callArguments, callPosition ->
                throw UnsupportedOperationException()
            }
        )
    }
}