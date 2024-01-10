package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test

class CustomBuiltinFunctionErrorTest {
    fun env() = ExecutionEnvironment().apply {
        registerFunction(CustomFunctionDefinition(
            receiverType = null,
            functionName = "myGlobalFunc",
            returnType = "String",
            parameterTypes = listOf(
                CustomFunctionParameter("a", "String"),
                CustomFunctionParameter("b", "Int"),
                CustomFunctionParameter("c", "String"),
            ),
            executable = { _, args ->
                val s = StringValue("${(args[0] as StringValue).value}|${(args[1] as IntValue).value + 100}|${(args[2] as StringValue).value}")
                println(s.value)
                s
            }
        ))
        registerFunction(CustomFunctionDefinition(
            receiverType = "String",
            functionName = "size",
            returnType = "Int",
            parameterTypes = listOf(CustomFunctionParameter("factor", "Int")),
            executable = { receiver, args ->
                IntValue((receiver as StringValue).value.length)
            }
        ))
    }

    @Test
    fun customGlobalFunctionWrongArgumentType() {
        assertSemanticFail("""
            val a = myGlobalFunc("aaaaa", "I AM WRONG", "b")
        """.trimIndent(), env())
    }

    @Test
    fun customGlobalFunctionExtraArgument1() {
        assertSemanticFail("""
            val a = myGlobalFunc("aaaaa", d = "wrong", "b")
        """.trimIndent(), env())
    }

    @Test
    fun customGlobalFunctionExtraArgument2() {
        assertSemanticFail("""
            val a = myGlobalFunc("aaaaa", 123, "b", "wrong")
        """.trimIndent(), env())
    }

    @Test
    fun customGlobalFunctionMissingArgument() {
        assertSemanticFail("""
            val a = myGlobalFunc("aaaaa", 123)
        """.trimIndent(), env())
    }

    @Test
    fun customGlobalFunctionDuplicateArgument() {
        assertSemanticFail("""
            val a = myGlobalFunc("aaaaa", 123, b = 456)
        """.trimIndent(), env())
    }

    @Test
    fun customExtensionFunctionWrongArgumentType() {
        assertSemanticFail("""
            val a = "string".size(1.23)
        """.trimIndent(), env())
    }

    @Test
    fun customExtensionFunctionMissingArgument() {
        assertSemanticFail("""
            val a = "string".size()
        """.trimIndent(), env())
    }

    @Test
    fun customExtensionFunctionExtraArgument() {
        assertSemanticFail("""
            val a = "string".size(1, 2)
        """.trimIndent(), env())
    }

    @Test
    fun customExtensionFunctionDuplicateArgument() {
        assertSemanticFail("""
            val a = "string".size(1, factor = 2)
        """.trimIndent(), env())
    }
}
