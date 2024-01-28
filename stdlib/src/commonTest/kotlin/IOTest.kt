import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class IOTest {

    @Test
    fun println1() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
        }
        val interpreter = interpreter("""
            println("Hello world!")
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        assertEquals("Hello world!\n", console.toString())
    }

    @Test
    fun println2() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
        }
        val interpreter = interpreter("""
            println("a")
            println("b")
            println("c")
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        assertEquals("a\nb\nc\n", console.toString())
    }
}
