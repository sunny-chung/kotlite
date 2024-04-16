package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

data class SourcePosition(val filename: String, val lineNum: Int, val col: Int, val index: Int) {
    companion object {
        val NONE = SourcePosition("", 1, 1)
        val BUILTIN = SourcePosition(BuiltinFilename.BUILTIN, 1, 1)
    }

    // make it compatible with interpreter:1.0.0-jvm
    constructor(filename: String, lineNum: Int, col: Int) : this(filename, lineNum, col, 0)

    override fun toString(): String {
        return "[$filename:$lineNum:$col]"
    }
}
