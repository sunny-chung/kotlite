package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ClassModifier
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DelegatedValueTest {

    class CustomClass(val value: Int) {
        override fun toString(): String {
            return "my toString ($value)"
        }

        companion object {
            fun create(value: CustomClass, symbolTable: SymbolTable) : DelegatedValue<CustomClass>
                = DelegatedValue<CustomClass>(value, clazz, symbolTable = symbolTable)

            val clazz = ProvidedClassDefinition(
                fullQualifiedName = "CustomClass",
                typeParameters = emptyList(),
                isInstanceCreationAllowed = true,
                primaryConstructorParameters = listOf(CustomFunctionParameter("value", "Int")),
                constructInstance = { interpreter, callArguments, callPosition ->
                    create(CustomClass((callArguments[0] as IntValue).value), interpreter.symbolTable())
                },
                modifiers = setOf(ClassModifier.open),
                position = SourcePosition("<Test>", 1, 1),
            )
        }
    }

    @Test
    fun toStringIsNotDelegatedIfNotExplicitlySpecified() {
        val env = ExecutionEnvironment().apply {
            registerClass(CustomClass.clazz)
        }
        val interpreter = interpreter("""
            val x = CustomClass(123)
            val y = CustomClass(45678)
            val a = x.toString()
            val b = "[${'$'}x]"
            val c = y.toString()
            val d = "[${'$'}y]"
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertNotEquals("my toString (123)", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertNotEquals("[my toString (123)]", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertNotEquals("my toString (45678)", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertNotEquals("[my toString (45678)]", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    fun overrideToStringByExtension() {
        val env = ExecutionEnvironment().apply {
            registerClass(CustomClass.clazz)
            registerFunction(CustomFunctionDefinition(
                position = SourcePosition.NONE,
                receiverType = "CustomClass",
                functionName = "toString",
                returnType = "String",
                parameterTypes = emptyList(),
                executable = { interpreter, receiver, args, typeArgs ->
                    StringValue((receiver as DelegatedValue<*>).value.toString(), interpreter.symbolTable())
                }
            ))
        }
        val interpreter = interpreter("""
            val x = CustomClass(123)
            val y = CustomClass(45678)
            val a = x.toString()
            val b = "[${'$'}x]"
            val c = y.toString()
            val d = "[${'$'}y]"
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("my toString (123)", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("[my toString (123)]", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("my toString (45678)", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("[my toString (45678)]", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    fun overrideOriginalToString() {
        val env = ExecutionEnvironment().apply {
            registerClass(CustomClass.clazz)
            registerFunction(CustomFunctionDefinition(
                position = SourcePosition.NONE,
                receiverType = "CustomClass",
                functionName = "toString",
                returnType = "String",
                parameterTypes = emptyList(),
                executable = { interpreter, receiver, args, typeArgs ->
                    StringValue((receiver as DelegatedValue<*>).value.toString(), interpreter.symbolTable())
                }
            ))
        }
        val interpreter = interpreter("""
            class MyClass(val value: Int) : CustomClass(value) {
                override fun toString(): String {
                    return if (value > 1000) {
                        super.toString()
                    } else {
                        "overridden ${'$'}value"
                    }
                }
            }
            val x = MyClass(123)
            val y = MyClass(45678)
            val a = x.toString()
            val b = "[${'$'}x]"
            val c = y.toString()
            val d = "[${'$'}y]"
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("overridden 123", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("[overridden 123]", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("my toString (45678)", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("[my toString (45678)]", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }
}
