package com.sunnychung.lib.multiplatform.kotlite.extension

import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.RepeatedType
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterType

fun DataType.unboxTypeParameterType(): DataType {
    return if (this is TypeParameterType) {
        val isNullable = this.isNullable || this.upperBound.isNullable
        this.upperBound.copyOf(isNullable = isNullable)
    } else {
        this
    }
}

fun DataType.unboxRepeatedType(currentScope: SymbolTable): DataType {
    return if (this is RepeatedType) {
        currentScope.typeNodeToDataType(
            Parser(
                Lexer(
                    BuiltinFilename.BUILTIN,
                    this.realTypeDescriptiveName
                )
            ).type()
        ) ?: this
    } else {
        this
    }
}
