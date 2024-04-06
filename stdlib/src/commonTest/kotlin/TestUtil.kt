import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.error.TypeMismatchException
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NumberValue
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

fun interpreter(code: String, isDebug: Boolean = false, executionEnvironment: ExecutionEnvironment = ExecutionEnvironment()) = Parser(
    Lexer("<Test>", code)
).let { parser ->
    val it = parser.script()
    if (isDebug) {
        println("AST:\n---\nflowchart TD\n${it.toMermaid()}\n---")
    }
    SemanticAnalyzer(it, executionEnvironment).analyze()
    if (isDebug) {
        println(CodeGenerator(it, isPrintDebugInfo = true).generateCode())
    }
    Interpreter(it, executionEnvironment)
}

fun semanticAnalyzer(code: String, environment: ExecutionEnvironment = ExecutionEnvironment()) = SemanticAnalyzer(
    rootNode = Parser(Lexer("<Test>", code)).script(),
    executionEnvironment = environment
)

fun assertSemanticFail(code: String, environment: ExecutionEnvironment = ExecutionEnvironment()) {
    assertFailsWith<SemanticException> {
        val a = semanticAnalyzer(code, environment)
        try {
            a.analyze()
        } catch (e: SemanticException) {
            println(e.message)
            throw e
        }
    }
}

fun assertTypeCheckFail(code: String, environment: ExecutionEnvironment = ExecutionEnvironment()) {
    assertFailsWith<TypeMismatchException> {
        val a = semanticAnalyzer(code, environment)
        try {
            a.analyze()
        } catch (e: TypeMismatchException) {
            println(e.message)
            throw e
        }
    }
}

fun compareNumber(expected: Number, actual: NumberValue<*>) {
    // check both because the expression `1.2345 is Int` in JS evaluates to true
    if (expected is Int && actual is IntValue) {
        assertEquals(expected, (actual as IntValue).value)
    } else {
        expected as Double
        assertTrue((actual as DoubleValue).value in (expected - 0.0001 .. expected + 0.0001))
    }
}
