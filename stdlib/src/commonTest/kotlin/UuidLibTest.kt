import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.ByteLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CoreLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.TextLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.UuidLibModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UuidLibTest {

    @Test
    fun generateUuidV4() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
            install(CollectionsLibModule())
            install(TextLibModule())
            install(ByteLibModule())
            install(UuidLibModule())
        }
        val interpreter = interpreter("""
            val uuids = mutableSetOf<String>()
            repeat(20000) {
                uuids += uuid4().toString()
            }
            val n = uuids.size
            val f = uuids.first()
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(20000, (symbolTable.findPropertyByDeclaredName("n") as IntValue).value)
        assertEquals(36, (symbolTable.findPropertyByDeclaredName("f") as StringValue).value.length)
        assertTrue("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex()
            .matches((symbolTable.findPropertyByDeclaredName("f") as StringValue).value))
    }

    @Test
    fun generateUuidV4Shorthand() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
            install(CollectionsLibModule())
            install(TextLibModule())
            install(ByteLibModule())
            install(UuidLibModule())
        }
        val interpreter = interpreter("""
            val uuids = mutableSetOf<String>()
            repeat(20000) {
                uuids += uuidString()
            }
            val n = uuids.size
            val f = uuids.first()
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(20000, (symbolTable.findPropertyByDeclaredName("n") as IntValue).value)
        assertEquals(36, (symbolTable.findPropertyByDeclaredName("f") as StringValue).value.length)
        assertTrue("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex()
            .matches((symbolTable.findPropertyByDeclaredName("f") as StringValue).value))
    }
}
