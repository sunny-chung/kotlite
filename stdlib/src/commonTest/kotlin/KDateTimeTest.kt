import com.sunnychung.lib.multiplatform.kdatetime.KInstant
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.KDateTimeLibModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KDateTimeTest {

    @Test
    fun kInstant() {
        val env = ExecutionEnvironment().apply {
            install(KDateTimeLibModule())
        }
        val interpreter = interpreter("""
            val t = KInstant.now()
            val s = t.format("yyyy-MM-dd E HH:mm aa Z")
            val m = t.toEpochMilliseconds()
            
            val t2 = KInstant(1705677172000)
            val s2 = t2.format(KDateTimeFormat.ISO8601_DATETIME.pattern)
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(KInstant.now().format("yyyy-MM-dd E HH:mm aa Z"), (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
        assertTrue(KInstant.now().toEpochMilliseconds() - (symbolTable.findPropertyByDeclaredName("m") as LongValue).value < 1000)
        assertEquals("2024-01-19T15:12:52Z", (symbolTable.findPropertyByDeclaredName("s2") as StringValue).value)
    }

    @Test
    fun kZonedInstantAndKZonedDateTime() {
        val env = ExecutionEnvironment().apply {
            install(KDateTimeLibModule())
        }
        val interpreter = interpreter("""
            val t: KZonedInstant = KZonedInstant.parseFromIso8601String("2023-09-15T17:18:53-07:00")
            val t2 = t.dropZoneOffset().atZoneOffset(KZoneOffset(9, 0))
            val s2 = t2.format(KDateTimeFormat.ISO8601_DATETIME.pattern)
            
            val t3: KZonedDateTime = t2.toKZonedDateTime().copy(hour = 23, minute = 10, second = 0, millisecond = 0)
            val s3 = t3.toKZonedInstant().format(KDateTimeFormat.ISO8601_DATETIME.pattern)
            val day = t3.day
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("2023-09-16T09:18:53+09:00", (symbolTable.findPropertyByDeclaredName("s2") as StringValue).value)
        assertEquals("2023-09-16T23:10:00+09:00", (symbolTable.findPropertyByDeclaredName("s3") as StringValue).value)
        assertEquals(16, (symbolTable.findPropertyByDeclaredName("day") as IntValue).value)
    }

    @Test
    fun kDuration() {
        val env = ExecutionEnvironment().apply {
            install(KDateTimeLibModule())
        }
        val interpreter = interpreter("""
            val d = 95.seconds()
            val s = d.format("m'm' s's'")
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("1m 35s", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }
}
