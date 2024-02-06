package com.sunnychung.lib.multiplatform.kotlite.util

import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeArguments
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeToUpperBound
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyAccessorsNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyType
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.typeUpperBoundOrAny

class ClassMemberResolver(val clazz: ClassDefinition, val typeArguments: List<TypeNode>?) {
    // [n-1] = clazz; [n-2] = superclass of clazz; etc.
    val genericResolutions: List<Pair<ClassDefinition, Map<String, TypeNode>>>
    val genericUpperBounds: List<Pair<ClassDefinition, Map<String, TypeNode>>>

    init {
        val genericResolutions: MutableList<Pair<ClassDefinition, Map<String, TypeNode>>> = mutableListOf()
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
        this.genericResolutions = genericResolutions.toList()
    }

    init {
        val genericUpperBounds: MutableList<Pair<ClassDefinition, Map<String, TypeNode>>> = mutableListOf()
        genericUpperBounds += clazz to clazz.typeParameters.mapIndexed { index, tp ->
            tp.name to (typeArguments?.get(index) ?: tp.typeUpperBoundOrAny())
        }.toMap()
        var clazz = clazz
        while (clazz.superClass != null) {
            val superClass = clazz.superClass!!
            val typeArguments = clazz.superClassInvocation!!.typeArguments
            genericUpperBounds += superClass to superClass.typeParameters.mapIndexed { index, tp ->
                tp.name to typeArguments[index].resolveGenericParameterTypeArguments(genericUpperBounds.last().second)
            }.toMap()

            clazz = superClass
        }
        genericUpperBounds.reverse()
        this.genericUpperBounds = genericUpperBounds.toList()
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

    private fun Pair<FunctionDeclarationNode, Int>?.resolveTypes(): FunctionAndTypes? {
        val (function, index) = this ?: return null

        val resolutionTable = genericResolutions[index].second
            .toMutableMap()
            .apply {
                function.typeParameters.forEach {
                    remove(it.name)
                }
            }
            .toMap()

        val type = function.returnType.let { type ->
            type.resolveGenericParameterTypeArguments(resolutionTable)
        }
        return FunctionAndTypes(
            function = function,
            resolvedValueParameterTypes = function.valueParameters.map {
                it.copy(declaredType = it.declaredType!!.resolveGenericParameterTypeArguments(resolutionTable))
            },
            resolvedReturnType = type,
            classTreeIndex = index,
        )
    }

    fun findMemberFunctionWithTypeByTransformedName(memberName: String): FunctionAndTypes? {
        return clazz.findMemberFunctionWithIndexByTransformedName(memberName)
            .resolveTypes()
    }

    fun findMemberFunctionWithIndexByTransformedNameLinearSearch(memberName: String): FunctionAndTypes? {
        return clazz.findMemberFunctionWithIndexByTransformedNameLinearSearch(memberName)
            .resolveTypes()
    }

    fun findMemberFunctionsAndTypeUpperBoundsByDeclaredName(memberName: String): Map<String, FunctionAndTypes> {
        val lookup: Map<String, Pair<FunctionDeclarationNode, Int>> = clazz.findMemberFunctionsWithIndexByDeclaredName(memberName)
        return lookup.mapValues {
            val upperBounds = genericUpperBounds[it.value.second].second.map {
                TypeParameterNode(it.key, it.value)
            }
            FunctionAndTypes(
                function = it.value.first,
                resolvedValueParameterTypes = it.value.first.valueParameters.map {
                    it.copy(declaredType = it.declaredType!!.resolveGenericParameterTypeToUpperBound(upperBounds))
                },
                resolvedReturnType = it.value.first.returnType.resolveGenericParameterTypeToUpperBound(upperBounds),
                classTreeIndex = it.value.second,
            )
        }
    }
}

data class FunctionAndTypes(
    val function: FunctionDeclarationNode,
    val resolvedValueParameterTypes: List<FunctionValueParameterNode>,
    val resolvedReturnType: TypeNode,
    val classTreeIndex: Int,
)
