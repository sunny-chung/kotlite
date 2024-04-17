package com.sunnychung.lib.multiplatform.kotlite.model

data class Token(val type: TokenType, val value: Any, val position: SourcePosition, val endExclusive: SourcePosition) {
    constructor(type: TokenType, value: Any, position: SourcePosition) : this(type, value, position, position.copy(index = position.index + value.toString().length))
}
