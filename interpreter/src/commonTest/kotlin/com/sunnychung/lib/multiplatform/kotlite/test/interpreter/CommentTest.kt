package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class CommentTest {

    @Test
    fun lineComment() {
        val interpreter = interpreter("""
            // abc
            val x: Int = 1 + 2// def
            //ghi
            
            
            
            val y: Int = 4 * 5 // jk
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun blockComments() {
        val interpreter = interpreter("""
            // abc
            /*
                Block
                Comment
            */
            val x: Int = 1 + 2// def
            //ghi
            val y: Int = 4/**
             *  Block Comment
             **/* 5 // jk
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }
}
