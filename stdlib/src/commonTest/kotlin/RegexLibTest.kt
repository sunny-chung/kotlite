import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.stdlib.RegexLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.TextLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class RegexLibTest {
    @Test
    fun matches() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
            install(RegexLibModule())
        }
        val interpreter = interpreter("""
            val s = "012233a"
            val a = s.matches(Regex("[0-9]+[a-z]"))
            val b = s.matches(Regex("[0-9]+"))
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
    }
}
