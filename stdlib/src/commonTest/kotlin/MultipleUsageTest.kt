import com.sunnychung.lib.multiplatform.kotlite.KotliteInterpreter
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.KDateTimeLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.RangeLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class MultipleUsageTest {

    @Test
    fun reuseLibrary() {
        run {
            val interpreter = KotliteInterpreter(
                filename = "<Test>",
                code = """
                    var i = 0
                    var sum = 0
                    while (++i <= 10) {
                        sum += i
                    }
                """.trimIndent(),
                executionEnvironment = ExecutionEnvironment().apply {
                    install(CollectionsLibModule())
                }
            )
            interpreter.eval()
            val symbolTable = interpreter.symbolTable()
            assertEquals(55, (symbolTable.findPropertyByDeclaredName("sum") as IntValue).value)
        }

        // continue to eval with a different env

        run {
            val interpreter = KotliteInterpreter(
                filename = "<Test>",
                code = """
                    var i = 0
                    var sum = 0
                    while (++i <= 10) {
                        sum += i
                    }
                """.trimIndent(),
                executionEnvironment = ExecutionEnvironment().apply {
                    install(KDateTimeLibModule())
                    install(CollectionsLibModule())
                }
            )
            interpreter.eval()
            val symbolTable = interpreter.symbolTable()
            assertEquals(55, (symbolTable.findPropertyByDeclaredName("sum") as IntValue).value)
        }

        // continue to eval with a different env

        run {
            val interpreter = KotliteInterpreter(
                filename = "<Test>",
                code = """
                    var sum = 0
                    for (i in (1..10)) {
                        sum += i
                    }
                """.trimIndent(),
                executionEnvironment = ExecutionEnvironment().apply {
                    install(CollectionsLibModule())
                    install(RangeLibModule())
                }
            )
            interpreter.eval()
            val symbolTable = interpreter.symbolTable()
            assertEquals(55, (symbolTable.findPropertyByDeclaredName("sum") as IntValue).value)
        }
    }
}
