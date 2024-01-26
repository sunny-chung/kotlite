package com.sunnychung.lib.multiplatform.kotlite.model

enum class ScopeType {
    Script, Function, Closure, Initializer, ClassInitializer /* for Analyzer and Runtime use */, Class, ClassMemberFunction,
    FunctionBlock, FunctionParameters,
    ExtraWrap,
    If, For, While, DoWhile;

    companion object {
//        fun isLoop(type: ScopeType) = type in setOf(For, While, DoWhile)
        fun ScopeType.isLoop() = this in setOf(For, While, DoWhile)
    }
}
