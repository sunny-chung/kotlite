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
            l.forEach { println(it) }
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
                .map { it * 2 }
                .map { "(${'$'}it)" }
                .forEach { println(it) }
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
                .filter { it <= 3 }
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
        val interpreter = interpreter("""
            val a = listOf(1, 2, 3, 4, 5)
                .mapIndexed { index, it -> "(${'$'}index: ${'$'}{ it * 2 })" }
                .takeLast(3)
                .joinToString(separator = "\n") { it -> ">${'$'}it" }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(">(2: 6)\n>(3: 8)\n>(4: 10)", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
    }

    @Test
    fun getOperator() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val a = listOf(1, 2, 3, 4, 5)
                .map { it * 2 }
            val a0 = a[0]
            val a1 = a[1]
            val a2 = a[2]
            val a3 = a[3]
            val a4 = a[4]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a0") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("a3") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a4") as IntValue).value)
    }

    @Test
    fun setOperatorOnNonMutableListShouldFail() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        assertSemanticFail("""
            val a = listOf(1, 2, 3, 4, 5)
                .map { it * 2 }
            a[1] = 2
        """.trimIndent(), environment = env)
    }

    @Test
    fun setOperatorOnMutableList() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val a = mutableListOf(1, 2, 3, 4, 5)
                .map { it * 2 }
                .toMutableList()
            
            a[1] = 5
            a[2] += 10
            a[a.lastIndex] *= 3
            val a0 = a[0]
            val a1 = a[1]
            val a2 = a[2]
            val a3 = a[3]
            val a4 = a[4]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a0") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals(16, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("a3") as IntValue).value)
        assertEquals(30, (symbolTable.findPropertyByDeclaredName("a4") as IntValue).value)
    }
}
