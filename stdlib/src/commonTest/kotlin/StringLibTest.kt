import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.TextLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

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
            val c = b.substring(2).uppercase()
            val d = '1'.isDigit()
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("asBabc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("BABC", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }

    @Test
    fun someTextFunctionsWithParameterNameSameAsExtensionProperty() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
        }
        val interpreter = interpreter("""
            val a = "abaBabc"
            val b = a.replace("ba", "s")
            val c = b.substring(2).uppercase().padStart(length = 5)
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(" BABC", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
    }

    @Test
    fun someTextExtensionProperty() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
        }
        val interpreter = interpreter("""
            val a = "abaBabc"
            val b = a.length
            val c = a.lastIndex
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(7, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun toInt() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
        }
        val interpreter = interpreter("""
            val a = "1234"
            val b = a.toInt() + 123
            val c = "567".toInt() + 12
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(1357, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(579, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun toBoolean() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
        }
        val interpreter = interpreter("""
            val a: String? = null
            val b = a.toBoolean()
            val c = "true".toBoolean()
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
    }

    @Test
    fun length() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
        }
        val interpreter = interpreter("""
            val a = "abcdef"
            val b = a.length
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
