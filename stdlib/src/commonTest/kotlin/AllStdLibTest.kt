import com.sunnychung.lib.multiplatform.kotlite.KotliteInterpreter
import com.sunnychung.lib.multiplatform.kotlite.MermaidFlowchartDirection
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.evalKotliteExpression
import com.sunnychung.lib.multiplatform.kotlite.kotliteAstNodeMermaidDiagram
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.AllStdLibModules
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AllStdLibTest {

    @Test
    fun all() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(AllStdLibModules(outputToConsoleFunction = {
                console.append(it)
            }))
        }
        val interpreter = KotliteInterpreter(
            filename = "<Test>",
            code = """
                println("Hello world!")
                val a = (1..10).fold(0) { acc, it -> acc + it }
            """.trimIndent(),
            executionEnvironment = env
        )
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(55, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("Hello world!\n", console.toString())
    }

    @Test
    fun expression() {
        val env = ExecutionEnvironment().apply {
            install(AllStdLibModules())
        }
        val result: IntValue = evalKotliteExpression(
            filename = "Calculate",
            code = "(1..10).fold(0) { acc, it -> acc + it }",
            executionEnvironment = env,
        ) as IntValue
        assertEquals(55, result.value)
    }

    @Test
    fun noMutableListSuccess() {
        val env = ExecutionEnvironment(
            classRegistrationFilter = {
                it != "MutableList"
            },
            functionRegistrationFilter = {
                !(it.receiverType ?: "").contains("MutableList") && !it.returnType.contains("MutableList")
            },
        ).apply {
            install(AllStdLibModules())
        }
        val interpreter = KotliteInterpreter(
            filename = "<Test>",
            code = """
                val a = (1..10).fold(0) { acc, it -> acc + it }
            """.trimIndent(),
            executionEnvironment = env
        )
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(55, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun noMutableListFail() {
        val env = ExecutionEnvironment(
            classRegistrationFilter = {
                it != "MutableList"
            },
            functionRegistrationFilter = {
                !(it.receiverType ?: "").contains("MutableList") && !it.returnType.contains("MutableList")
            },
        ).apply {
            install(AllStdLibModules())
        }
        assertFailsWith<SemanticException> {
            KotliteInterpreter(
                filename = "<Test>",
                code = """
                    val a = mutableListOf(1, 2, 3)
                """.trimIndent(),
                executionEnvironment = env
            )
        }
    }

    @Test
    fun mutableMapLong() {
        val code = """
            val cache = mutableMapOf<Int, Long>()
            fun fib(i: Int): Long {
                if (i < 0) throw Exception("Invalid i: ${'$'}i")
                if (i <= 1) return i.toLong()
                if (i in cache) {
                    return cache[i]!!
                }
                return (fib(i - 2) + fib(i - 1)).also {
                    cache[i] = it
                }
            }
            val a = fib(19) // 4181L
        """.trimIndent()
        println(kotliteAstNodeMermaidDiagram(code, MermaidFlowchartDirection.LeftToRight))

        val env = ExecutionEnvironment().apply {
            install(AllStdLibModules())
        }
        val interpreter = KotliteInterpreter(filename = "Fib", code = code, executionEnvironment = env)
        interpreter.eval()

        val symbolTable = interpreter.symbolTable()
        assertEquals(4181L, (symbolTable.findPropertyByDeclaredName("a") as LongValue).value)
    }
}
