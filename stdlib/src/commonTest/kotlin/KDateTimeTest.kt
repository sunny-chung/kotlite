import com.sunnychung.lib.multiplatform.kdatetime.KInstant
import com.sunnychung.lib.multiplatform.kdatetime.extension.hours
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
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
            val d = 1.minutes() + 15.seconds() + 20000.milliseconds()
            val s = d.format("m'm' s's'")
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("1m 35s", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test // TODO uncomment
    fun plusMinusOperatorsAndInfix() {
        val env = ExecutionEnvironment().apply {
            install(KDateTimeLibModule())
        }
        val interpreter = interpreter("""
            val dateTime = KZonedDateTime(
                year = 2024,
                month = 1,
                day = 1,
                hour = 1,
                minute = 8,
                second = 40,
                zoneOffset = KZoneOffset.parseFrom("+08:00")
            )
            val yesterday: KZonedDateTime = dateTime - 1.days()
            val theDayAfterTomorrow = dateTime + 2.days()
//            val duration: KDuration = theDayAfterTomorrow - yesterday
            val a = yesterday.format("yy MM-dd H:mm:ss aa (z)")
//            val b = duration.toSeconds()
            
            val t1: KZonedInstant = KInstant(1710250706001) at KZoneOffset(-7, 0)
            val t2: KInstant = KInstant(1710250716000)
            val c = (t1 - t2).toMilliseconds()
            val d: Long = t1.zoneOffset.toMilliseconds()
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("23 12-31 1:08:40 am (+08:00)", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
//        assertEquals(86400 * 3, (symbolTable.findPropertyByDeclaredName("b") as LongValue).value)
        assertEquals(-9999, (symbolTable.findPropertyByDeclaredName("c") as LongValue).value)
        assertEquals(-7.hours().toMilliseconds(), (symbolTable.findPropertyByDeclaredName("d") as LongValue).value)
    }

    @Test
    fun comparisonAndSorting() {
        val env = ExecutionEnvironment().apply {
            install(KDateTimeLibModule())
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val t1: KZonedInstant = KInstant(1710250706001) at KZoneOffset(-7, 0)
            val t2: KInstant = KInstant(1710250716000)
            val t3: KZonedInstant = KInstant(1610250706001) at KZoneOffset(8, 0)
            val a = t1 < t2
            val b = t2 < t3
            val l = listOf(t1, t2, t3).sorted()
            val c = l[2] >= l[1]
            val d = l[1] > l[0]
            
            val d1 = 3.milliseconds()
            val d2 = 4.milliseconds()
            val e = d1 > d2
            val f = d1 < d2
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
    }
}
