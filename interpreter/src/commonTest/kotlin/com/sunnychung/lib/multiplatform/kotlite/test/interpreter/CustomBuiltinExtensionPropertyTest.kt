package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.IntType
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis.assertSemanticFail
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomBuiltinExtensionPropertyTest {

    @Test
    fun getter() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "f",
                receiver = "String",
                type = "Int",
                getter = { subject ->
                    IntValue((subject as StringValue).value.length * 10)
                }
            ))
        }
        val interpreter = interpreter("""
            val s = "abcde"
            val a = "fgh".f
            val b = s.f
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(30, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(50, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun setter() {
        var hostScopeVariable = 0
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "f",
                receiver = "String",
                type = "Int",
                setter = { subject, value ->
                    hostScopeVariable += (value as IntValue).value
                }
            ))
        }
        val interpreter = interpreter("""
            val s = "abcde"
            s.f = 10
            s.f = 15
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(25, hostScopeVariable)
    }

    @Test
    fun getterAndSetter() {
        var hostScopeVariable = 0
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "f",
                receiver = "String",
                type = "Int",
                getter = { subject ->
                    IntValue(hostScopeVariable)
                },
                setter = { subject, value ->
                    hostScopeVariable += (value as IntValue).value
                }
            ))
        }
        val interpreter = interpreter("""
            val s = "abcde"
            s.f = 10
            val a = s.f
            s.f = 15
            val b = s.f
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(25, hostScopeVariable)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun callNonExistSetter() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "f",
                receiver = "String",
                type = "Int",
                getter = { subject ->
                    IntValue((subject as StringValue).value.length * 10)
                }
            ))
        }
        assertSemanticFail("""
            val s = "abcde"
            s.f = 10
        """.trimIndent(), environment = env)
    }

    @Test
    fun callNonExistGetter() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "f",
                receiver = "String",
                type = "Int",
                setter = { subject, value ->

                }
            ))
        }
        assertSemanticFail("""
            val s = "abcde"
            val a = s.f
        """.trimIndent(), environment = env)
    }
}
