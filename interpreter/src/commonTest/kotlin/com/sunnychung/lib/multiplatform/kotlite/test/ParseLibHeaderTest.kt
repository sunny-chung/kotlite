package com.sunnychung.lib.multiplatform.kotlite.test

import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParseLibHeaderTest {
    @Test
    fun parseFunctionHeaders() {
        val headers = parser("""
            fun String.count(): Int
            fun String.drop(n: Int): String
            fun String.dropLast(n: Int): String
        """.trimIndent()).libHeaderFile()
        assertEquals(3, headers.size)
        headers.forEach {
            assertTrue(it is FunctionDeclarationNode)
            assertEquals("String", it.receiver?.descriptiveName())
        }
    }

    @Test
    fun parseFunctionHeadersWithDefaultValue() {
        val headers = parser("""
            fun String.contains(other: Char, ignoreCase: Boolean = false): Boolean
            fun String.count(): Int
        """.trimIndent()).libHeaderFile()
        assertEquals(2, headers.size)
        headers.forEach {
            assertTrue(it is FunctionDeclarationNode)
            assertEquals("String", it.receiver?.descriptiveName())
        }
    }

    @Test
    fun parseExtensionProperties() {
        val headers = parser("""
            val String.length: Int
                get()
            
            val String.lastIndex: Int
                get()
        """.trimIndent()).libHeaderFile()
        assertEquals(2, headers.size)
        headers.forEach {
            assertTrue(it is PropertyDeclarationNode)
            assertEquals("String", it.receiver?.descriptiveName())
            assertTrue(it.accessors?.getter != null)
        }
    }

    @Test
    fun parseExtensionPropertiesAndFunctions() {
        val headers = parser("""
            val String.length: Int
                get()
            
            val String.lastIndex: Int
                get()
                
            fun String.contains(other: Char, ignoreCase: Boolean = false): Boolean
            fun String.count(): Int
        """.trimIndent()).libHeaderFile()
        assertEquals(4, headers.size)
        headers.take(2).forEach {
            assertTrue(it is PropertyDeclarationNode)
            assertEquals("String", it.receiver?.descriptiveName())
            assertTrue(it.accessors?.getter != null)
        }
        headers.subList(2, 4).forEach {
            assertTrue(it is FunctionDeclarationNode)
            assertEquals("String", it.receiver?.descriptiveName())
        }
    }
}