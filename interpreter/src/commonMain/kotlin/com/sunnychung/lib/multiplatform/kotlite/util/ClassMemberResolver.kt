package com.sunnychung.lib.multiplatform.kotlite.util

import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeArguments
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyAccessorsNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyType
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode

class ClassMemberResolver(val clazz: ClassDefinition, val typeArguments: List<TypeNode>?) {
    // [n-1] = clazz; [n-2] = superclass of clazz; etc.
    val genericResolutions: MutableList<Pair<ClassDefinition, Map<String, TypeNode>>> = mutableListOf()

    init {
        genericResolutions += clazz to clazz.typeParameters.mapIndexed { index, tp ->
            tp.name to (typeArguments?.get(index) ?: TypeNode(tp.name, null, false))
        }.toMap()
        var clazz = clazz
        while (clazz.superClass != null) {
            val superClass = clazz.superClass!!
            val typeArguments = clazz.superClassInvocation!!.typeArguments
            genericResolutions += superClass to superClass.typeParameters.mapIndexed { index, tp ->
                tp.name to typeArguments[index].resolveGenericParameterTypeArguments(genericResolutions.last().second)
            }.toMap()

            clazz = superClass
        }
        genericResolutions.reverse()
    }

    fun findMemberPropertyCustomAccessorWithType(memberName: String): Pair<PropertyAccessorsNode, TypeNode>? {
        val (accessor, index) = clazz.findMemberPropertyCustomAccessorWithIndex(memberName) ?: return null
        val type = accessor.type.let { type ->
            type.resolveGenericParameterTypeArguments(genericResolutions[index].second)
        }
        return accessor to type
    }

    fun findMemberPropertyWithoutAccessorWithType(memberName: String): Pair<PropertyType, TypeNode>? {
        val (property, index) = clazz.findMemberPropertyWithoutAccessorWithIndex(memberName) ?: return null
        val type = property.type.let { type ->
            type.toTypeNode().resolveGenericParameterTypeArguments(genericResolutions[index].second)
        }
        return property to type
    }
}
