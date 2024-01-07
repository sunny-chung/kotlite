package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.error.TypeMismatchException
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFailsWith

fun semanticAnalyzer(code: String) = SemanticAnalyzer(Parser(Lexer(code)).script())
fun assertSemanticFail(code: String) {
    val a = semanticAnalyzer(code)
    assertFailsWith<SemanticException> { a.analyze() }
}
fun assertTypeCheckFail(code: String) {
    val a = semanticAnalyzer(code)
    assertFailsWith<TypeMismatchException> { a.analyze() }
}

class SemanticAnalyzerTest {
    fun build(code: String) = SemanticAnalyzer(
        Parser(Lexer(code)).script()
    )

    @Test
    fun ok1() {
        build("""
            val x: Int = 1 + 2
            val y: Int = 5 + 4 * (7 + 3) - 1
            val z: Int = x * 2 + y
            var a: Int = z
            a -= y - 4
            a %= x
        """.trimIndent()).analyze()
    }

    @Test
    fun ok2() {
        build("""
            val x: Int = 1 + 2
            val y: Int = 5 + 4 * (7 + 3) - 1
            val z: Int = x * 2 + y
            var a: Int = z
            a -= a - 4
            a %= a
        """.trimIndent()).analyze()
    }

    @Test
    fun nonExistVariable1() {
        val analyzer = build("""
            val x: Int = 1 + 2
            val y: Int = 5 + 4 * (7 + q) - 1
            val z: Int = x * 2 + y
            var a: Int = z
            a -= y - 4
            a %= x
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun nonExistVariable2() {
        val analyzer = build("""
            val x: Int = 1 + 2
            val y: Int = 5 + 4 * (7 + y) - 1
            val z: Int = x * 2 + y
            var a: Int = z
            a -= y - 4
            a %= x
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun duplicateVariable() {
        val analyzer = build("""
            val x: Int = 1 + 2
            val y: Int = 5 + 4 * (7 + y) - 1
            val z: Int = x * 2 + y
            var x: Int = z - 1
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun simpleFunction1() {
        val analyzer = build("""
            val x: Int = 1 + 2
            fun funcA(a: Int, b: Int): Unit {
                val c: Int = x + a * b
            }
            val y: Int = x - 5
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun simpleFunction2() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int, b: Int) {
                val c: Int = a * b
            }
            val y: Int = a - 5
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun simpleFunction3() {
        val analyzer = build("""
            val x: Int = 1 + 2
            fun funcA(a: Int, b: Int = a + 2) {
                val c: Int = a * b
            }
            val y: Int = x - 5
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun simpleFunction4() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int = a + 5, b: Int) {
                val c: Int = a * b
            }
            val y: Int = a - 5
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun simpleFunction5() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int = a + 5, b: Int = a) {
                val c: Int = a * b
            }
            fun funcB(a: Int = a + 5, b: Int = a + 1) {
                val c: Int = a * b
            }
            val y: Int = a - 5
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun simpleFunctionFail1() {
        val analyzer = build("""
            val x: Int = 1 + 2
            fun funcA(a: Int, b: Int) {
                val c: Int = x + a * b
            }
            val y: Int = x - 5 + b
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun simpleFunctionFail2() {
        val analyzer = build("""
            val x: Int = 1 + 2
            fun funcA(a: Int, b: Int) {
                val c: Int = x + a * b - c
            }
            val y: Int = x - 5
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun simpleFunctionFail3() {
        val analyzer = build("""
            val x: Int = 1 + 2
            fun funcA(a: Int = a + 9, b: Int) {
                val c: Int = x + a * b
            }
            val y: Int = x - 5
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun simpleFunctionShadowParameter() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int = a + 9, b: Int) {
                val a: Int = 1 + a * b
            }
            val y: Int = a - 5
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun simpleFunctionFail4() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int = a + 9, a: Int) {
                val b: Int = 1 + a
            }
            val y: Int = a - 5
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun recursion() {
        val analyzer = build("""
            val a: Int = 1
            fun factorial(x: Int): Int {
                if (x < 1) {
                    return a
                }
                return x * factorial(x - 1)
            }
            val y: Int = factorial(10)
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun recursionFail() {
        val analyzer = build("""
            fun factorial(x: Int): Int {
                if (x < 1) {
                    return a
                }
                return x * factorial(x - 1)
            }
            val a: Int = 1
            val y: Int = factorial(10)
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun nestedFunctions() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int = a + 5, b: Int = a) {
                val x: Int = 10
                fun funcB(a: Int = a + 5, b: Int = a + 1, c: Int) {
                    val d: Int = a * b + c * x
                }
            
                val c: Int = a * b
            }
            val y: Int = a - 5
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun nestedFunctions2() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int = a + 5, b: Int = a) {
                val x: Int = 10
                fun funcB(a: Int = a + 5, b: Int = a + 1, c: Int) {
                    val d: Int = a * b + c * x
                }
            
                val c: Int = a * b
                funcB(1, 2, 3)
                return
            }
            val y: Int = a - 5
            funcA(1, 2)
        """.trimIndent())
        analyzer.analyze()
    }

    @Test
    fun nestedFunctionsIncorrectParameterAccess() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int = a + 5, b: Int = a) {
                val x: Int = 10
                fun funcB(a: Int = a + 5, b: Int = a + 1, c: Int) {
                    val d: Int = a * b + c * x
                }
            
                val e: Int = a * b + c
            }
            val y: Int = a - 5
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun nestedFunctionsCannotBeAccessedByOuterScope() {
        val analyzer = build("""
            val a: Int = 1 + 2
            fun funcA(a: Int = a + 5, b: Int = a) {
                val x: Int = 10
                fun funcB(a: Int = a + 5, b: Int = a + 1, c: Int) {
                    val d: Int = a * b + c * x
                }
            
                val e: Int = a * b
            }
            val y: Int = a - 5
            funcB(1, 2, 3)
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun functionCallArgumentIncorrectAccess() {
        val analyzer = build("""
            fun sum(a: Int, b: Int): Int {
                return a + b
            }
            sum(1, y)
            val y: Int = 2
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun returnOutsideFunction() {
        val analyzer = build("""
            fun sum(a: Int, b: Int): Int {
                return a + b
            }
            sum(1, 2)
            return 3
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun breakOutsideLoop() {
        val analyzer = build("""
            val x: Int = 1 + 2
            break
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun continueOutsideLoop() {
        val analyzer = build("""
            val x: Int = 1 + 2
            continue
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun duplicateFunctionDeclaration1() {
        val analyzer = build("""
            fun myFunction() {
                val a: Int = b + c
            }
            fun myFunction() {
                val d: Int = e + f
            }
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun duplicateFunctionDeclaration2() {
        val analyzer = build("""
            fun myFunction() {
                val a: Int = b + c
            }
            fun myFunction2() {
                fun myFunction() {
                    val d: Int = e + f
                }
            }
        """.trimIndent())
        assertFailsWith<SemanticException> {
            analyzer.analyze()
        }
    }

    @Test
    fun primaryConstructorParametersOrPropertiesCannotBeReassigned() {
        assertSemanticFail("""
            class Cls(var a: Int = 10, var e: Int = 60, var f: Int = e++) {
                var b: Int = ++a
                var c: Int = (++a) + (++b)
                var d: Int = b++
            }
            val o: Cls = Cls()
        """.trimIndent())
    }

    @Test
    fun stringFieldReferenceToNonExistField() {
        assertSemanticFail("""
            val s: String = "abc${'$'}x.def"
            val x: Int = 30
        """.trimIndent())
    }

    @Test
    fun stringInterpolationReferenceToNonExistField() {
        assertSemanticFail("""
            val s: String = "abc${'$'}{x * 2 + 1}def"
            val x: Int = 30
        """.trimIndent())
    }

    @Test
    fun multilineStringFieldReferenceToNonExistField() {
        assertSemanticFail("""
            val s: String = ${"\"\"\""}
                multiline
                abc${'$'}x.def
            ${"\"\"\""}
            val x: Int = 30
        """.trimIndent())
    }

    @Test
    fun multilineStringInterpolationReferenceToNonExistField() {
        assertSemanticFail("""
            val s: String = ${"\"\"\""}
                abc${'$'}{x * 2 + 1}def
            ${"\"\"\""}
            val x: Int = 30
        """.trimIndent())
    }

    @Test
    fun underscoreArgumentCannotBeAccessed() {
        assertSemanticFail("""
            fun f(g: ((Int, Int, Int) -> Int)? = null): Int {
                if (g == null) {
                    return 1
                }
                return g(2, 5, 11, 19)
            }
            val x = f { x, _, y ->
                x + _ + y
            }
        """.trimIndent())
    }
}