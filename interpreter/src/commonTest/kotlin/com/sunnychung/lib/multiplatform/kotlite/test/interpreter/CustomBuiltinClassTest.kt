package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValueHolder
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringType
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis.assertSemanticFail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CustomBuiltinClassTest {

    class ConstructableClassValue(var value: String, symbolTable: SymbolTable) : ClassInstance(
        currentScope = symbolTable,
        clazz = clazz,
        fullClassName = clazz.fullQualifiedName,
        typeArguments = emptyList(),
    ) {
        companion object {
            val clazz = ProvidedClassDefinition(
                fullQualifiedName = "ConstructableClass",
                typeParameters = emptyList(),
                isInstanceCreationAllowed = true,
                primaryConstructorParameters = listOf(CustomFunctionParameter("field", "String")),
                constructInstance = { interpreter, callArguments, callPosition ->
                    return@ProvidedClassDefinition ConstructableClassValue(
                        value = (callArguments[0] as StringValue).value,
                        symbolTable = interpreter.symbolTable()
                    )
                },
                position = SourcePosition("<Test>", 1, 1),
            )
        }
    }

    val unconstructableClass: ProvidedClassDefinition = ProvidedClassDefinition(
        fullQualifiedName = "UnconstructableClass",
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ ->
            throw UnsupportedOperationException()
        },
        position = SourcePosition("<Test>", 1, 1),
    )

    val someInterface: ProvidedClassDefinition = ProvidedClassDefinition(
        fullQualifiedName = "SomeInterface",
        isInterface = true,
        typeParameters = emptyList(),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ ->
            throw UnsupportedOperationException()
        },
        position = SourcePosition("<Test>", 1, 1),
    )

    @Test
    fun createInstanceOfCustomClass() {
        val env = ExecutionEnvironment().apply {
            registerClass(ConstructableClassValue.clazz)
            registerExtensionProperty(
                ExtensionProperty(
                    receiver = "ConstructableClass",
                    declaredName = "a",
                    type = "String",
                    getter = { interpreter, receiver, typeArgs ->
                        StringValue((receiver as ConstructableClassValue).value)
                    },
                    setter = { interpreter, receiver, value, typeArgs ->
                        (receiver as ConstructableClassValue).value = (value as StringValue).value
                    }
                )
            )
        }
        val interpreter = interpreter("""
            val x = ConstructableClass("aaaa")
            val a = x.a
            x.a = "abc"
            val b = x.a
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("aaaa", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun createInstanceOfUnconstructableCustomClassShouldFail() {
        val env = ExecutionEnvironment().apply {
            registerClass(unconstructableClass)
        }
        assertSemanticFail("""
            val x = UnconstructableClass()
        """.trimIndent(), environment = env)
    }

    @Test
    fun customTypeDefineInterfaceAsSuperClassShouldFail() {
        assertFailsWith<SemanticException> {
            ProvidedClassDefinition(
                fullQualifiedName = "FailClass",
                typeParameters = emptyList(),
                isInstanceCreationAllowed = false,
                primaryConstructorParameters = emptyList(),
                constructInstance = { _, _, _ ->
                    throw UnsupportedOperationException()
                },
                superClassInvocation = "SomeInterface()",
                superClass = someInterface,
                position = SourcePosition("<Test>", 1, 1),
            )
        }
    }

    @Test
    fun customTypeDefineClassAsSuperInterfaceShouldFail() {
        assertFailsWith<SemanticException> {
            ProvidedClassDefinition(
                fullQualifiedName = "FailClass",
                typeParameters = emptyList(),
                isInstanceCreationAllowed = false,
                primaryConstructorParameters = emptyList(),
                constructInstance = { _, _, _ ->
                    throw UnsupportedOperationException()
                },
                superInterfaceTypeNames = listOf("UnconstructableClass"),
                superInterfaces = listOf(unconstructableClass),
                position = SourcePosition("<Test>", 1, 1),
            )
        }
    }

    @Test
    fun defineSuperClassWithoutInvocationShouldFail() {
        assertFailsWith<SemanticException> {
            ProvidedClassDefinition(
                fullQualifiedName = "FailClass",
                typeParameters = emptyList(),
                isInstanceCreationAllowed = false,
                primaryConstructorParameters = emptyList(),
                constructInstance = { _, _, _ ->
                    throw UnsupportedOperationException()
                },
                superClass = unconstructableClass,
                position = SourcePosition("<Test>", 1, 1),
            )
        }
    }

    @Test
    fun defineSuperClassInvocationWithoutClassDefinitionShouldFail() {
        assertFailsWith<SemanticException> {
            ProvidedClassDefinition(
                fullQualifiedName = "FailClass",
                typeParameters = emptyList(),
                isInstanceCreationAllowed = false,
                primaryConstructorParameters = emptyList(),
                constructInstance = { _, _, _ ->
                    throw UnsupportedOperationException()
                },
                superClassInvocation = "UnconstructableClass()",
                position = SourcePosition("<Test>", 1, 1),
            )
        }
    }

    @Test
    fun defineSuperInterfaceWithoutTypeNameShouldFail() {
        assertFailsWith<SemanticException> {
            ProvidedClassDefinition(
                fullQualifiedName = "FailClass",
                typeParameters = emptyList(),
                isInstanceCreationAllowed = false,
                primaryConstructorParameters = emptyList(),
                constructInstance = { _, _, _ ->
                    throw UnsupportedOperationException()
                },
                superInterfaces = listOf(someInterface),
                position = SourcePosition("<Test>", 1, 1),
            )
        }
    }

    @Test
    fun defineSuperInterfaceTypeNameWithoutInterfaceDefinitionShouldFail() {
        assertFailsWith<SemanticException> {
            ProvidedClassDefinition(
                fullQualifiedName = "FailClass",
                typeParameters = emptyList(),
                isInstanceCreationAllowed = false,
                primaryConstructorParameters = emptyList(),
                constructInstance = { _, _, _ ->
                    throw UnsupportedOperationException()
                },
                superInterfaceTypeNames = listOf("SomeInterface"),
                position = SourcePosition("<Test>", 1, 1),
            )
        }
    }
}
