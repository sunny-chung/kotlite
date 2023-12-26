package com.sunnychung.lib.multiplatform.kotlite.model

class CallStack {

    private val activationRecords = mutableListOf<ActivationRecord>()

    init {
        activationRecords += ActivationRecord(
            functionFullQualifiedName = ":builtin",
            callPosition = SourcePosition(1, 1),
            scopeType = ScopeType.Script,
            parent = null,
            scopeLevel = 0
        )
        activationRecords += ActivationRecord(
            functionFullQualifiedName = ":global",
            callPosition = SourcePosition(1, 1),
            scopeType = ScopeType.Script,
            parent = activationRecords.last(),
            scopeLevel = 1
        )
    }

    fun getStacktrace(): List<String> {
        return if (activationRecords.size < 3) {
            emptyList()
        } else {
            return activationRecords.subList(2, activationRecords.size)
                .asReversed()
                .map { "${it.functionFullQualifiedName} (${it.callPosition.lineNum}:${it.callPosition.col})" }
        }
    }

    fun push(functionFullQualifiedName: String?, scopeType: ScopeType, callPosition: SourcePosition) {
        activationRecords += ActivationRecord(
            functionFullQualifiedName = functionFullQualifiedName,
            callPosition = callPosition,
            parent = activationRecords.last(),
            scopeLevel = activationRecords.size,
            scopeType = scopeType,
        )
    }

    fun pop(scopeType: ScopeType) {
        val ar = activationRecords.removeLast()
        if (ar.scopeType != scopeType) {
            throw IllegalStateException("A wrong scope is completed")
        }
    }

    fun currentSymbolTable() = activationRecords.last().symbolTable
}
