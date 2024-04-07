import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.HTMLTextAreaElement

fun main() {
    val lstExample = document.getElementById("lstExample")!!
    val txtCode = document.getElementById("txtCode") as HTMLTextAreaElement

    demoScripts.forEach { demoScriptItem ->
        val item = document.createElement("div")
        item.addClass("item")
        item.textContent = demoScriptItem.key
        item.addEventListener("click", {
            txtCode.value = demoScriptItem.value
        })
        lstExample.appendChild(item)
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
            val result: String = interpretKotlite(code)
            println("onClick 3")
            btnRun.removeClass("loading")
            (document.getElementById("txtOutput") as HTMLTextAreaElement).value = result
            println("onClick 4")
        }, 10)
    })
    println("main end")
}
