package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.test.interpreter.interpreter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PropertyImmutableTest {

    @Test
    fun outerScopeSuccess1() {
        val interpreter = interpreter(
            """
            val a: Int = 3
            var b: Int = 4
            b = 5
        """.trimIndent()
        )
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun outerScopeSuccess2() {
        val interpreter = interpreter(
            """
            val a: Int
            var b: Int
            b = 4
            a = 3
            b = 5
        """.trimIndent()
        )
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun outerScopeFail1() {
        val analyzer = semanticAnalyzer(
            """
            val a: Int = 3
            var b: Int = 4
            b = 5
            a = 6
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun outerScopeFail2() {
        val analyzer = semanticAnalyzer(
            """
            val a: Int
            var b: Int
            b = 4
            a = 3
            b = 5
            a = 6
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun outerScopeFail3() {
        val analyzer = semanticAnalyzer(
            """
            val a: Int
            var b: Int
            b = 4
            a = 3
            b += 5
            a += 6
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun outerScopeUnary() {
        val analyzer = semanticAnalyzer(
            """
            val a: Int
            var b: Int
            b = 4
            a = 3
            a++
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun blockFail1() {
        val analyzer = semanticAnalyzer(
            """
            if (true) {
                val a: Int = 2
                a = 30
            }
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun blockFail2() {
        val analyzer = semanticAnalyzer(
            """
            val a: Int = 2
            if (true) {
                a = 30
            }
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun functionArgumentFail() {
        val analyzer = semanticAnalyzer(
            """
            fun f(a: Int = 20) {
                a = 30
            }
            f()
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun functionModifyOutsideFail() {
        val analyzer = semanticAnalyzer(
            """
            val a: Int = 2
            fun f() {
                a = 30
            }
            f()
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun classPrimaryConstructorFail1() {
        val analyzer = semanticAnalyzer(
            """
            class Cls(c: Int = 10, var a: Int = 60, var b: Int = c++)
            Cls()
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun classPrimaryConstructorFail2() {
        val analyzer = semanticAnalyzer(
            """
            class Cls(var a: Int = 60, var b: Int = a++)
            Cls()
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun classInitFail1() {
        val analyzer = semanticAnalyzer(
            """
            class Cls(var a: Int = 60) {
                val b: Int = 10
                val c: Int = ++b
            }
            Cls()
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun classInitFail2() {
        val analyzer = semanticAnalyzer(
            """
            class Cls(var a: Int = 60) {
                val b: Int = 10
                init {
                    b = 20
                }
            }
            Cls()
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun classFunctionArgument() {
        val analyzer = semanticAnalyzer(
            """
            class Cls(var a: Int = 60) {
                val b: Int = 10
                fun f(c: Int) {
                    c = 20
                }
            }
            Cls().f(1)
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun classFunction() {
        val analyzer = semanticAnalyzer(
            """
            class Cls(var a: Int = 60) {
                val b: Int = 10
                fun f() {
                    b = 20
                }
            }
            Cls().f()
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }

    @Test
    fun memberValProperty1() {
        assertFailsWith<SemanticException> {
            interpreter("""
                class MyCls {
                    val a: Int = 1
                }
                val o: MyCls = MyCls()
                o.a = 20
            """.trimIndent()).eval()
        }
    }

    @Test
    fun memberValProperty2() {
        assertFailsWith<SemanticException> {
            interpreter("""
                class MyCls {
                    val a: Int = 1
                    fun f() {
                        a = 2
                    }
                }
                val o: MyCls = MyCls()
                o.f()
            """.trimIndent()).eval()
        }
    }

    @Test
    fun memberValProperty3() {
        assertFailsWith<SemanticException> {
            interpreter("""
                class MyCls {
                    val a: Int = 1
                    fun f() {
                        this.a = 2
                    }
                }
                val o: MyCls = MyCls()
                o.f()
            """.trimIndent()).eval()
        }
    }

    @Test
    fun memberPropertySetterArgument() {
        val analyzer = semanticAnalyzer(
            """
            var a: Int = -10
            class MyCls {
                var a: Int = 1
                var b: Int
                    set(value) {
                        value = 30
                        a += value
                    }
            }
            val o: MyCls = MyCls()
            val x: Int = o.a
            o.b = 20
        """.trimIndent()
        )
        assertFailsWith<SemanticException> { analyzer.analyze() }
    }
}