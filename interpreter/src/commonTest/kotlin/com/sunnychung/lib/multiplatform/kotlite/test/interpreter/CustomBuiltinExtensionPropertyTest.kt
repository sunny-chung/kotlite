package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter
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
                getter = { interpreter, subject, typeArgs ->
                    IntValue((subject as StringValue).value.length * 10, interpreter.symbolTable())
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
                setter = { interpreter, subject, value, typeArgs ->
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
                getter = { interpreter, subject, typeArgs ->
                    IntValue(hostScopeVariable, interpreter.symbolTable())
                },
                setter = { interpreter, subject, value, typeArgs ->
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
                getter = { interpreter, subject, typeArgs ->
                    IntValue((subject as StringValue).value.length * 10, interpreter.symbolTable())
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
                setter = { interpreter, subject, value, typeArgs ->

                }
            ))
        }
        assertSemanticFail("""
            val s = "abcde"
            val a = s.f
        """.trimIndent(), environment = env)
    }

    @Test
    fun duplicateExtensionProperty() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                receiver = "String",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                receiver = "String",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
        }
        assertSemanticFail(code = "", environment = env)
    }

    @Test
    fun duplicateExtensionPropertyOfGenericClass1() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                receiver = "List<Any>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                receiver = "List<Any>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
        }
        assertSemanticFail(code = "", environment = env)
    }

    @Test
    fun duplicateExtensionPropertyOfGenericClass2() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                typeParameters = listOf(TypeParameter("T", "Any")),
                receiver = "List<T>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                typeParameters = listOf(TypeParameter("T", "Any")),
                receiver = "List<T>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
        }
        assertSemanticFail(code = "", environment = env)
    }

    @Test
    fun duplicateExtensionPropertyOfGenericClass3() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                receiver = "List<Any?>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                typeParameters = listOf(TypeParameter("T", "Any?")),
                receiver = "List<T>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
        }
        assertSemanticFail(code = "", environment = env)
    }

    @Test
    fun extensionPropertyOfGenericClassAndMoreSpecificExtensionProperty() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                typeParameters = listOf(TypeParameter("T", "Any?")),
                receiver = "List<T>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(1, interpreter.symbolTable()) }
            ))
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                receiver = "List<Int>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(2, interpreter.symbolTable()) }
            ))
        }
        val interpreter = interpreter("""
            fun <T> list(vararg values: T): List<T> = values
            val a = list(1, 2).prop
            val b = list("abC").prop
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionPropertyOfNull() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                typeParameters = listOf(TypeParameter("K", "Any?"), TypeParameter("V", "Any?")),
                receiver = "Pair<K, V>",
                type = "Int",
                getter = { interpreter, subject, typeArgs -> IntValue(10, interpreter.symbolTable()) }
            ))
        }
        val interpreter = interpreter("""
            fun f(x: Int) = if (x > 0) Pair(123.4, "56") else null
            val a = f(1)?.prop ?: -5
            val b = f(-1)?.prop ?: -5
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun accessNullableExtensionPropertyMemberWithDot() {
        val env = ExecutionEnvironment().apply {
            registerExtensionProperty(ExtensionProperty(
                declaredName = "prop",
                receiver = "Int",
                type = "Pair<Int, Double>?",
                getter = { interpreter, subject, typeArgs -> NullValue }
            ))
        }
        assertSemanticFail("""
            val a = 1.prop.second
        """.trimIndent(), environment = env)
    }
}
