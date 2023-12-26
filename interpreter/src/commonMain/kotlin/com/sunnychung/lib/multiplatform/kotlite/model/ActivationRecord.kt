package com.sunnychung.lib.multiplatform.kotlite.model

class ActivationRecord(val functionFullQualifiedName: String?, val scopeType: ScopeType, val callPosition: SourcePosition, private val parent: ActivationRecord?, private val scopeLevel: Int,) {
    val symbolTable: SymbolTable = SymbolTable(
        scopeLevel = scopeLevel,
        scopeName = functionFullQualifiedName ?: "<anonymous>",
        scopeType = scopeType,
        parentScope = parent?.symbolTable
    )
}
