package com.sunnychung.lib.multiplatform.kotlite.util

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeArguments
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeToUpperBound
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyAccessorsNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyType
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.typeUpperBoundOrAny

class ClassMemberResolver private constructor(symbolTable: SymbolTable, private val clazz: ClassDefinition, private val typeArguments: List<TypeNode>?) {
    companion object {
        /**
         * If `createOnlyIfClassExists` == true, returns a resolver only if `clazz` can be found in `symbolTable`; null otherwise.
         * If `createOnlyIfClassExists` == false, always returns a resolver.
         */
        fun create(symbolTable: SymbolTable, clazz: ClassDefinition, typeArguments: List<TypeNode>?, createOnlyIfClassExists: Boolean = false): ClassMemberResolver? {
            if (createOnlyIfClassExists && symbolTable.findClass(clazz.fullQualifiedName) == null) {
                return null
            }
            return ClassMemberResolver(symbolTable, clazz, typeArguments)
        }
    }

    // [n-1] = clazz; [n-2] = superclass of clazz; etc.
    @Deprecated("use genericResolutionsByTypeName") val genericResolutions: List<Pair<ClassDefinition, Map<String, TypeNode>>>
    @Deprecated("use genericUpperBoundsByTypeName") val genericUpperBounds: List<Pair<ClassDefinition, Map<String, TypeNode>>>

    // key = type full name, value = { key = type parameter name of that type, value = type argument }
    val genericResolutionsByTypeName: Map<String, Map<String, TypeNode>>
    val genericUpperBoundsByTypeName: Map<String, Map<String, TypeNode>>
    val typeDefinitions: Map<String, ClassDefinition>

    init {
        val genericResolutions: MutableList<Pair<ClassDefinition, Map<String, TypeNode>>> = mutableListOf()

        val genericResolutionsByTypeName: MutableMap<String, Map<String, TypeNode>> = mutableMapOf()
        val genericUpperBoundsByTypeName: MutableMap<String, Map<String, TypeNode>> = mutableMapOf()
        val typeDefinitions: MutableMap<String, ClassDefinition> = mutableMapOf()

        val symbolTable = SymbolTable(
            scopeLevel = symbolTable.scopeLevel + 1,
            scopeName = "ClassMemberResolver",
            scopeType = ScopeType.ExtraWrap,
            parentScope = symbolTable,
            isInitOnCreate = false, // don't init. primitive types would fail to be resolved when initializing root symbol table
        )

        fun checkAndPutResolution(typeDef: ClassDefinition, resolvedTypeArguments: Map<String, TypeNode>, destination: MutableMap<String, Map<String, TypeNode>>) {
            if (destination.containsKey(typeDef.name)) {
                val previousResolution = destination[typeDef.name]!!
                if (resolvedTypeArguments == null || previousResolution.size != resolvedTypeArguments.size) {
                    throw RuntimeException("Inconsistent type argument resolution for type ${typeDef.name} -- ${previousResolution} VS ${resolvedTypeArguments}")
                }
                val changes = mutableMapOf<String, TypeNode>()
                resolvedTypeArguments.forEach {
                    if (previousResolution[it.key] == null) {
                        throw RuntimeException("Inconsistent type argument `${it.key}` resolution for type ${typeDef.name}")
                    }
                    val previousResolvedType = symbolTable.assertToDataType(changes[it.key] ?: previousResolution[it.key]!!)
                    val currentResolvedType = symbolTable.assertToDataType(it.value)
                    if (previousResolvedType.isConvertibleFrom(currentResolvedType)) {
                        if (!currentResolvedType.isConvertibleFrom(previousResolvedType)) {
                            changes[it.key] = it.value
                        }
                    } else {
                        throw SemanticException(it.value.position, "Type argument ${it.value.descriptiveName()} for type parameter ${it.key} of type ${typeDef.name} is in conflict with ${previousResolvedType.descriptiveName}")
                    }
                }
                if (changes.isNotEmpty()) {
                    destination[typeDef.name] = destination[typeDef.name]!! + changes
                }
            } else {
                destination[typeDef.name] = resolvedTypeArguments
                typeDefinitions[typeDef.name] = typeDef
            }
        }

        fun visitInterface(parent: ClassDefinition) {
            val types = parent.superInterfaceTypes.groupBy { it.name }
            types.forEach {
                if (it.value.size > 1) {
                    throw SemanticException(it.value[1].position, "Duplicated definition of interface inheritance")
                }
            }
            parent.superInterfaces.forEach { def ->
                val type = types[def.fullQualifiedName]?.firstOrNull()
                    ?: throw RuntimeException("Super type ${def.fullQualifiedName} is not defined but appears in superInterfaces")
                val typeArguments = def.typeParameters.mapIndexed { index, tp ->
                    if (type.arguments == null || type.arguments.lastIndex < index) {
                        throw SemanticException(type.position, "Type argument ${tp.name} for type ${type.name} is not defined")
                    }
                    tp.name to type.arguments[index].resolveGenericParameterTypeArguments(genericResolutionsByTypeName[parent.fullQualifiedName]!!)
                }.toMap()
                val upperBounds = def.typeParameters.mapIndexed { index, tp ->
                    tp.name to type.arguments!![index].resolveGenericParameterTypeArguments(genericUpperBoundsByTypeName[parent.fullQualifiedName]!!)
                }.toMap()
                checkAndPutResolution(def, typeArguments, genericResolutionsByTypeName)
                checkAndPutResolution(def, upperBounds, genericUpperBoundsByTypeName)
                visitInterface(def)
            }
        }

        if (typeArguments == null) {
            clazz.typeParameters.map { it.name to it.typeUpperBoundOrAny() }.forEach {
                symbolTable.declareTypeAlias(SourcePosition.NONE, it.first, it.second)
            }
        }

        val firstTypeArgumentMap = clazz.typeParameters.mapIndexed { index, tp ->
            tp.name to (typeArguments?.get(index) ?: TypeNode(tp.position, tp.name, null, false))
        }.toMap()
        val firstTypeArgumentUpperBoundMap = clazz.typeParameters.mapIndexed { index, tp ->
            tp.name to (typeArguments?.get(index) ?: tp.typeUpperBoundOrAny())
        }.toMap()
        genericResolutions += clazz to firstTypeArgumentMap
        checkAndPutResolution(clazz, firstTypeArgumentMap, genericResolutionsByTypeName)
        checkAndPutResolution(clazz, firstTypeArgumentUpperBoundMap, genericUpperBoundsByTypeName)
        visitInterface(clazz)
        var clazz = clazz
        while (clazz.superClass != null) {
            val superClass = clazz.superClass!!
            val typeArguments = clazz.superClassInvocation!!.typeArguments
            val typeArgumentMap = superClass.typeParameters.mapIndexed { index, tp ->
                tp.name to typeArguments[index].resolveGenericParameterTypeArguments(genericResolutionsByTypeName[clazz.fullQualifiedName]!!)
            }.toMap()
            genericResolutions += superClass to typeArgumentMap
            checkAndPutResolution(superClass, typeArgumentMap, genericResolutionsByTypeName)
            val upperBoundMap = superClass.typeParameters.mapIndexed { index, tp ->
                tp.name to typeArguments[index].resolveGenericParameterTypeArguments(genericUpperBoundsByTypeName[clazz.fullQualifiedName]!!)
            }.toMap()
            checkAndPutResolution(superClass, upperBoundMap, genericUpperBoundsByTypeName)
            visitInterface(superClass)

            clazz = superClass
        }
        genericResolutions.reverse()

        this.genericResolutions = genericResolutions.toList()
        this.genericResolutionsByTypeName = genericResolutionsByTypeName.toMap()
        this.genericUpperBoundsByTypeName = genericUpperBoundsByTypeName.toMap()
        this.typeDefinitions = typeDefinitions.toMap()
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

    fun forEachSuperClassesFromRoot(visit: (index: Int, clazz: ClassDefinition, typeResolutions: Map<String, TypeNode>, typeUpperBounds: Map<String, TypeNode>) -> Unit) {
        genericResolutions.forEachIndexed { index, (clazz, typeResolutions) ->
            val upperBounds = genericUpperBounds[index].second
            visit(index, clazz, typeResolutions, upperBounds)
        }
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

//    private fun Pair<FunctionDeclarationNode, Int>?.resolveTypes(): FunctionAndTypes? {
//        val (function, index) = this ?: return null
//
//        val resolutionTable = genericResolutions[index].second
//            .toMutableMap()
//            .apply {
//                function.typeParameters.forEach {
//                    remove(it.name)
//                }
//            }
//            .toMap()
//
//        val type = function.returnType.let { type ->
//            type.resolveGenericParameterTypeArguments(resolutionTable)
//        }
//        return FunctionAndTypes(
//            function = function,
//            resolvedValueParameterTypes = function.valueParameters.map {
//                it.copy(declaredType = it.declaredType!!.resolveGenericParameterTypeArguments(resolutionTable))
//            },
//            resolvedReturnType = type,
////            classTreeIndex = index,
//            enclosingTypeName = "",
//        )
//    }

    fun resolveTypes(function: FunctionDeclarationNode, encloseTypeName: String): FunctionAndTypes {
        return Pair(function, encloseTypeName).resolveTypes()!!
    }

    private fun Pair<FunctionDeclarationNode, String>?.resolveTypes(): FunctionAndTypes? {
        val (function, encloseTypeName) = this ?: return null

        val resolutionTable = genericResolutionsByTypeName[encloseTypeName]!!
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
//            classTreeIndex = index,
            enclosingTypeName = encloseTypeName,
        )
    }

    fun findMemberFunctionWithTypeByTransformedName(memberName: String): FunctionAndTypes? {
        return clazz.findMemberFunctionWithEnclosingTypeNameByTransformedName(memberName)
            .resolveTypes()
    }

    fun findMemberFunctionWithIndexByTransformedNameLinearSearch(memberName: String): FunctionAndTypes? {
        return clazz.findMemberFunctionWithEnclosingTypeNameByTransformedNameLinearSearch(memberName)
            .resolveTypes()
    }

    fun findMemberFunctionsAndTypeUpperBoundsByDeclaredName(memberName: String): Map<String, FunctionAndTypes> {
        val lookup: Map<String, Pair<FunctionDeclarationNode, String>> = clazz.findMemberFunctionsWithEnclosingTypeNameByDeclaredName(memberName)
        return lookup.mapValues {
            val upperBounds = genericUpperBoundsByTypeName[it.value.second]!!.map {
                TypeParameterNode(it.value.position, it.key, it.value)
            }
            FunctionAndTypes(
                function = it.value.first,
                resolvedValueParameterTypes = it.value.first.valueParameters.map {
                    it.copy(declaredType = it.declaredType!!.resolveGenericParameterTypeToUpperBound(upperBounds))
                },
                resolvedReturnType = it.value.first.returnType.resolveGenericParameterTypeToUpperBound(upperBounds),
//                classTreeIndex = it.value.second,
                enclosingTypeName = it.value.second,
            )
        }
    }

    fun findMemberFunctionsAndExactTypesByDeclaredName(memberName: String, clazz: ClassDefinition = this.clazz): Map<String, FunctionAndTypes> {
        val lookup: Map<String, Pair<FunctionDeclarationNode, String>> = clazz.findMemberFunctionsWithEnclosingTypeNameByDeclaredName(memberName)
        return lookup.mapValues {
            it.value.resolveTypes()!!
        }
    }

    fun containsSuperType(typeName: String): Boolean {
        return genericResolutionsByTypeName.containsKey(typeName)
    }
}

data class FunctionAndTypes(
    val function: FunctionDeclarationNode,
    val resolvedValueParameterTypes: List<FunctionValueParameterNode>,
    val resolvedReturnType: TypeNode,
//    val classTreeIndex: Int,
    val enclosingTypeName: String,
)
