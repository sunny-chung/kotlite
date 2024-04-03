import com.sunnychung.lib.multiplatform.kotlite.model.ByteValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.ByteLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.TextLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteLibTest {

    @Test
    fun byteArrayOfForEachSizeLastIndex() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(TextLibModule())
            install(CollectionsLibModule())
            install(ByteLibModule())
        }
        val interpreter = interpreter("""
            val a: ByteArray = byteArrayOf(31.toByte(), 45.toByte(), 12.toByte(), 108.toByte())
            a.forEach { println(it) }
            val b: Int = a.size
            val c: Int = a.lastIndex
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals("31\n45\n12\n108\n", console.toString())
    }

    @Test
    fun encodeToByteArrayForLoop() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(TextLibModule())
            install(CollectionsLibModule())
            install(ByteLibModule())
        }
        val interpreter = interpreter("""
            for (b in "ABC".encodeToByteArray()) {
                println(b)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals("65\n66\n67\n", console.toString())
    }

    @Test
    fun encodeToByteArrayDecodeToString() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
            install(CollectionsLibModule())
            install(ByteLibModule())
        }
        val interpreter = interpreter("""
            val s = "abcABC123π_!\n^%#"
            val b = s.encodeToByteArray()
            val s2 = b.decodeToString()
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("abcABC123π_!\n^%#", (symbolTable.findPropertyByDeclaredName("s2") as StringValue).value)
    }

    @Test
    fun plusOperatorGetOperatorSetOperator() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
            install(CollectionsLibModule())
            install(ByteLibModule())
        }
        val interpreter = interpreter("""
            val arr: ByteArray = byteArrayOf(31.toByte(), 45.toByte(), 12.toByte()) + 108.toByte()
            val size = arr.size
            arr[0] = 61.toByte()
            arr[2] = 20.toByte()
            val a: Byte = arr[0]
            val b: Byte = arr[1]
            val c: Byte = arr[2]
            val d: Byte = arr[3]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(61, (symbolTable.findPropertyByDeclaredName("a") as ByteValue).value.toInt())
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("b") as ByteValue).value.toInt())
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("c") as ByteValue).value.toInt())
        assertEquals(108, (symbolTable.findPropertyByDeclaredName("d") as ByteValue).value.toInt())
    }

    @Test
    fun negativeByte() {
        val env = ExecutionEnvironment().apply {
            install(TextLibModule())
            install(CollectionsLibModule())
            install(ByteLibModule())
        }
        assertTypeCheckFail("""
            val b: Byte = -2.toByte() // same as Kotlin 1.9 behaviour. (-2).toByte() will work instead.
        """.trimIndent(), environment = env)
    }

    @Test
    fun copyOfFillSetToList() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(TextLibModule())
            install(CollectionsLibModule())
            install(ByteLibModule())
        }
        val interpreter = interpreter("""
            val a: ByteArray = byteArrayOf(31.toByte(), 45.toByte()).copyOf(7)
            a.fill((-2).toByte(), fromIndex = 3)
            a[5] = 127.toByte()
            val l: List<Byte> = a.toList()
            val b: Int = l.size
            l.forEach { println(it) }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(7, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("31\n45\n0\n-2\n-2\n127\n-2\n", console.toString())
    }
}
