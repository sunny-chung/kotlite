import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import kotlin.test.assertFailsWith

fun semanticAnalyzer(code: String, environment: ExecutionEnvironment = ExecutionEnvironment()) = SemanticAnalyzer(
    scriptNode = Parser(Lexer("<Test>", code)).script(),
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
