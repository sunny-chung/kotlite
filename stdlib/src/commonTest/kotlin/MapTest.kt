import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTest {
    @Test
    fun simpleRead() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val m = mapOf(
                "ab" to 123,
                "bcd" to 234,
                "e" to 345,
                "fghi" to 56789,
            )
            val size: Int = m.size
            val typeCheck1: Boolean = m is Map<String, Int>
            val typeCheck2: Boolean = m is Map<Int, String>
            val a: Int? = m["ab"]
            val b: Int? = m["bcd"]
            val c: Int? = m["e"]
            val d: Int? = m["fghi"]
            val e: Int? = m["notexist"]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2") as BooleanValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(56789, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun listToMap() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            class Data<A, B>(val key: A, val value: B)
            val m = listOf(
                Data("ab", 123),
                Data("bcd", 234),
                Data("e", 345),
                Data("fghi", 56789),
            ).map { Pair(it.key, it.value) }
            .toMap<String, Int>() // TODO remove diamand
            val size: Int = m.size
            val typeCheck1: Boolean = m is Map<String, Int>
            val typeCheck2: Boolean = m is Map<Int, String>
            val a: Int? = m["ab"]
            val b: Int? = m["bcd"]
            val c: Int? = m["e"]
            val d: Int? = m["fghi"]
            val e: Int? = m["notexist"]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2") as BooleanValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(56789, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun listAssociateToMap() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            class Data<A, B>(val key: A, val value: B)
            val m = listOf(
                Data("ab", 123),
                Data("bcd", 234),
                Data("e", 345),
                Data("fghi", 56789),
            ).associate { it.key to it.value }
            val size: Int = m.size
            val typeCheck1: Boolean = m is Map<String, Int>
            val typeCheck2: Boolean = m is Map<Int, String>
            val a: Int? = m["ab"]
            val b: Int? = m["bcd"]
            val c: Int? = m["e"]
            val d: Int? = m["fghi"]
            val e: Int? = m["notexist"]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2") as BooleanValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(56789, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun listGroupBy() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            class Data<A, B>(val key: A, val value: B)
            val m = listOf(
                Data("ab", 123),
                Data("bcd", 234),
                Data("e", 345),
                Data("ab", 3),
                Data("fghi", 56789),
                Data("e", 8),
                Data("e", 9),
                Data("fghi", 12),
            ).groupBy(
                keySelector = { it.key },
                valueSelector = { it.value },
            )
            val size: Int = m.size
            val typeCheck1: Boolean = m is Map<String, List<Int>>
            val typeCheck2: Boolean = m is Map<String, Int>
            val a: Int = m["ab"]?.size
            val b: Int = m["bcd"]?.size
            val c: Int = m["e"]?.size
            val d: Int = m["fghi"]?.size
            val e: Int = m["notexist"]?.size ?: -1
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2") as BooleanValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }
}
