package com.sunnychung.lib.multiplatform.kotlite.extension

import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun String.toDataType(symbolTable: SymbolTable) =
    Parser(Lexer("-", this)).type(isParseDottedIdentifiers = true, isIncludeLastIdentifierAsTypeName = true)
        .let { symbolTable.assertToDataType(it) }
