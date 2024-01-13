import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.TextLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

fun interpreter(code: String, isDebug: Boolean = false, executionEnvironment: ExecutionEnvironment = ExecutionEnvironment()) = Parser(
    Lexer(code)
).let { parser ->
    val it = parser.script()
    if (isDebug) {
        println("AST:\n---\nflowchart TD\n${it.toMermaid()}\n---")
    }
    SemanticAnalyzer(it, executionEnvironment).analyze()
    if (isDebug) {
        println(CodeGenerator(it, isPrintDebugInfo = false).generateCode())
    }
    Interpreter(it, executionEnvironment)
}

class StringLibTest {
    @Test
    fun someTextFunctions() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
        }
        val interpreter = interpreter("""
            val a = "abaBabc"
            val b = a.replace("ba", "s", true)
            val c = b.substring(1, 3).uppercase()
            val d = '1'.isDigit()
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("assbc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("SS", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }

    @Test
    fun someTextFunctionsWithDefaultValue() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
        }
        val interpreter = interpreter("""
            val a = "abaBabc"
            val b = a.replace("ba", "s")
            val c = b.substring(1, 3).uppercase()
            val d = '1'.isDigit()
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("asBabc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("SB", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }
}
