package com.sunnychung.lib.multiplatform.kotlite.model

/**
 * Currently, only cater types without type parameters
 *
 * TODO: This should be implemented as a stack, to reduce performance penalty of copying the cache all the time
 */
class SymbolTableTypeVisitCache(vararg initialTypeNames: String) {
    private val visitedTypes = mutableSetOf<String>(*initialTypeNames)
    private val typeResolution = mutableMapOf<String, DataType>()
    private val unprocessedRepeatedTypes = mutableMapOf<String, MutableList<RepeatedType>>()

    val isEmpty: Boolean
        get() = visitedTypes.isEmpty()

    fun isEligible(type: TypeNode) = type.arguments == null
    fun isEligible(type: ClassDefinition) = type.typeParameters.isEmpty()

    fun preVisit(type: TypeNode) {
        if (!isEligible(type)) return
        visitedTypes += type.name
    }

    fun preVisit(type: ClassDefinition) {
        if (!isEligible(type)) return
        visitedTypes += type.fullQualifiedName
    }

    fun isVisited(type: TypeNode): Boolean {
        return isEligible(type) && visitedTypes.contains(type.name)
    }

    fun isVisited(type: ClassDefinition): Boolean {
        return isEligible(type) && visitedTypes.contains(type.fullQualifiedName)
    }

    fun postVisit(type: TypeNode, resolution: DataType) {
        postVisit(type.name, resolution)
    }

    fun postVisit(type: ClassDefinition, resolution: DataType) {
        postVisit(type.fullQualifiedName, resolution)
    }

    fun postVisit(typeName: String, resolution: DataType) {
        if (resolution is RepeatedType) {
            // possible race conditions under multi-thread env
            if (typeResolution.containsKey(typeName)) {
                resolution.actualType = typeResolution[typeName]
            } else {
                unprocessedRepeatedTypes.getOrPut(typeName) { mutableListOf() } += resolution
            }
            return
        }

        if (!typeResolution.containsKey(typeName)) {
            unprocessedRepeatedTypes[typeName]?.also {
                it.forEach {
                    it.actualType = resolution
                } // possible race conditions under multi-thread env
                unprocessedRepeatedTypes.remove(typeName)
            }
        }
        typeResolution[typeName] = resolution
    }

    fun throwErrorIfThereIsUnprocessedRepeatedType() {
        if (unprocessedRepeatedTypes.isNotEmpty()) {
            throw RuntimeException("There is unprocessed repeated type: ${unprocessedRepeatedTypes.keys}")
        }
    }

    fun copy(): SymbolTableTypeVisitCache {
        return SymbolTableTypeVisitCache().also {
            it.visitedTypes.addAll(visitedTypes)
            it.typeResolution.putAll(typeResolution)
            unprocessedRepeatedTypes.forEach { e ->
                it.unprocessedRepeatedTypes[e.key] = e.value.toMutableList()
            }
        }
    }
}
