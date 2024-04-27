import com.sunnychung.lib.multiplatform.kotlite.KotliteInterpreter
import com.sunnychung.lib.multiplatform.kotlite.extension.fullClassName
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.stdlib.AllStdLibModules
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement

fun main() {
    val lstExample = document.getElementById("lstExample")!!
    val txtCode = document.getElementById("txtCode") as HTMLTextAreaElement
    val cotStdlibs = document.getElementById("cotStdlibs")!!
    val checkboxes: MutableMap<String, HTMLInputElement> = mutableMapOf()

    demoScripts.forEach { demoScriptItem ->
        val item = document.createElement("div")
        item.addClass("item")
        item.textContent = demoScriptItem.key
        item.addEventListener("click", {
            txtCode.value = demoScriptItem.value
        })
        lstExample.appendChild(item)
    }
    txtCode.value = demoScripts.entries.first().value

    AllStdLibModules().modules.forEach {
        val checkboxDiv = document.createElement("div").apply {
            addClass("field")
            appendChild(document.createElement("div").apply {
                addClass("ui", "checkbox")
                appendChild(document.createElement("input").apply {
                    this as HTMLInputElement
                    type = "checkbox"
                    id = "chk${it.name}"
                    checked = true
                    checkboxes[it.name] = this
                })
                appendChild(document.createElement("label").apply {
                    textContent = it.name
                })
            })
        }
        cotStdlibs.appendChild(checkboxDiv)
    }

    val btnRun = document.getElementById("btnRun")!!
    println("main start")
    btnRun.addEventListener("click", {
        println("onClick 1")
        val code: String = txtCode.value
        println("onClick 2 a")
        btnRun.addClass("loading")
        window.setTimeout({
            println("pre run")

            val resultBuilder = StringBuilder()
            val libraryModules = AllStdLibModules { out -> resultBuilder.append(out) }
                .modules
                .filter { checkboxes[it.name]?.checked == true }
            try {
                KotliteInterpreter("UserScript", code, ExecutionEnvironment().apply {
                    libraryModules.forEach {
                        install(it)
                    }
                }).eval()
            } catch (e: Throwable) {
                resultBuilder.append("[Error!] ${e.fullClassName}: ${e.message}")
            }

            val result: String = resultBuilder.toString()
            println("onClick 3")
            btnRun.removeClass("loading")
            (document.getElementById("txtOutput") as HTMLTextAreaElement).value = result
            println("onClick 4")
        }, 10)
    })
    println("main end")
}
