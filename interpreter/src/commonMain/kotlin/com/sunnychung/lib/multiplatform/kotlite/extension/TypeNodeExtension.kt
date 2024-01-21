package com.sunnychung.lib.multiplatform.kotlite.extension

import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

fun TypeNode.resolveGenericParameterType(typeParameters: List<TypeParameterNode>): TypeNode {
    if (typeParameters.isEmpty()) return this

    fun resolve(type: TypeNode): TypeNode {
        return if (type is FunctionTypeNode) {
            FunctionTypeNode(
                type.receiverType,
                type.parameterTypes?.map { resolve(it) },
                type.returnType?.let { resolve(it) },
                type.isNullable
            )
        } else {
            val genericParameter = typeParameters.firstOrNull { it.name == type.name }
            val genericResolvedType = if (genericParameter != null) {
                genericParameter.typeUpperBound ?: TypeNode("Any", null, isNullable = true)
            } else null
            TypeNode(
                genericResolvedType?.name ?: type.name,
                type.arguments?.map { resolve(it) },
                (genericResolvedType?.isNullable ?: false) || type.isNullable
            )
        }
    }
    return resolve(this)
}
