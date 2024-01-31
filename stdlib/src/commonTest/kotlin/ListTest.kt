import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ListTest {
    @Test
    fun forEach() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val l = listOf(1, 2, 3, 4, 5)
            l.forEach { it -> println(it) }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals("1\n2\n3\n4\n5\n", console.toString())
    }

    @Test
    fun map() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            listOf(1, 2, 3, 4, 5)
                .map { it -> it * 2 }
                .map { it -> "(${'$'}it)" }
                .forEach { it -> println(it) }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals("(2)\n(4)\n(6)\n(8)\n(10)\n", console.toString())
    }

    @Test
    fun filterAndSize() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val a = listOf(1, 2, 3, 4, 5)
                .shuffled()
                .filter { it -> it <= 3 }
                .size
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun mapIndexedAndTakeLastAndJoinToString() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        // TODO enhance the syntax sugar of joinToString
        val interpreter = interpreter("""
            val a = listOf(1, 2, 3, 4, 5)
                .mapIndexed { index, it -> "(${'$'}index: ${'$'}{ it * 2 })" }
                .takeLast(3)
                // .joinToString("\n") { it -> ">${'$'}it" }
                .joinToString(separator = "\n", transform = { it -> ">${'$'}it" })
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(">(2: 6)\n>(3: 8)\n>(4: 10)", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
    }
}
