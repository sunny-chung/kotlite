package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.annotation.ModifyByAnalyzer

class ExtensionProperty(
    val declaredName: String,
    val typeParameters: List<TypeParameter> = emptyList(),
    val receiver: String,
    val type: String,
    val getter: ((interpreter: Interpreter, receiver: RuntimeValue, typeArgs: Map<String, DataType>) -> RuntimeValue)? = null,
    val setter: ((interpreter: Interpreter, receiver: RuntimeValue, value: RuntimeValue, typeArgs: Map<String, DataType>) -> Unit)? = null,
) {
    init {
        if (getter == null && setter == null) {
            throw IllegalArgumentException("Missing getter or setter")
        }
    }

    internal @ModifyByAnalyzer var transformedName: String? = null
    internal @ModifyByAnalyzer var typeNode: TypeNode? = null
    internal @ModifyByAnalyzer var receiverType: TypeNode? = null

    fun typeArgumentsMap(actualReceiverType: DataType): Map<String, DataType> {
        return if (typeParameters.isNotEmpty()) {
            var type: DataType? = actualReceiverType
            while (type != null && type.name != receiverType!!.name) {
                type = (type as? ObjectType)?.superType
            }
            if (type == null || type !is ObjectType) {
                throw RuntimeException("Cannot find receiver type ${receiverType!!.name}")
            }
            type.clazz.typeParameters.mapIndexed { index, tp ->
                tp.name to type.arguments[index]
            }.toMap()
        } else {
            emptyMap()
        }
    }
}
