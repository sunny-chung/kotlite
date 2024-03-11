package com.sunnychung.lib.multiplatform.kotlite.extension

import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

fun TypeNode.resolveGenericParameterType(typeParameters: List<TypeParameterNode>): TypeNode {
    if (typeParameters.isEmpty()) return this
    return this
}

fun TypeNode.resolveGenericParameterTypeArguments(typeArguments: Map<String, TypeNode>): TypeNode {
    return resolveGenericParameterTypeToUpperBound(typeArguments.map {
        TypeParameterNode(it.value.position, it.key, it.value)
    })
}

fun TypeNode.resolveGenericParameterTypeToUpperBound(typeParameters: List<TypeParameterNode>, isKeepTypeParameter: Boolean = false): TypeNode {
    if (typeParameters.isEmpty()) return this

    fun resolve(type: TypeNode): TypeNode {
        return if (type is FunctionTypeNode) {
            FunctionTypeNode(
                type.position,
                type.receiverType,
                type.parameterTypes?.map { resolve(it) },
                type.returnType?.let { resolve(it) },
                type.isNullable
            )
        } else {
            // Cases:
            // 1. A => MyPair<B, C> (use `genericResolvedType.arguments`)
            // 2. MyPair<A, B> => MyPair<MyPair<C, D>, B> (use `type.arguments`)
            // 3. A<B, C> => (Kotlin: Type arguments are not allowed for type parameter)
            val genericParameter = typeParameters.firstOrNull { it.name == type.name }
            if (genericParameter != null) { // case 1
                val genericResolvedType = genericParameter.typeUpperBound
                    ?: if (isKeepTypeParameter) {
                        TypeNode(SourcePosition.NONE, type.name, null, isNullable = false)
                    } else {
                        TypeNode(SourcePosition.NONE, "Any", null, isNullable = true)
                    }
                TypeNode(
                    genericResolvedType.position,
                    genericResolvedType.name,
                    genericResolvedType.arguments/*?.map { resolve(it) }*/, // resolving generic type's argument may cause infinite loop, but it is not needed as there is no case 3
                    genericResolvedType.isNullable || type.isNullable
                )
            } else { // case 2
                TypeNode(
                    type.position,
                    type.name,
                    type.arguments?.map { resolve(it) },
                    type.isNullable
                )
            }
        }
    }
    return resolve(this)
}
