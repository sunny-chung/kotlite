package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

class MutableListValue(value: MutableList<RuntimeValue>, typeArgument: DataType) : DelegatedValue<MutableList<RuntimeValue>>(value, clazz, listOf(typeArgument)) {

    companion object {
        val clazz = ProvidedClassDefinition(
            fullQualifiedName = "MutableList",
            typeParameters = listOf(TypeParameterNode(name = "T", typeUpperBound = null)),
            isInstanceCreationAllowed = false,
            primaryConstructorParameters = emptyList(),
            constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        )
    }
}
