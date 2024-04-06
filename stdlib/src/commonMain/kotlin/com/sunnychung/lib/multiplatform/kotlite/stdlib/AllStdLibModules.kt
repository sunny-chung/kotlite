package com.sunnychung.lib.multiplatform.kotlite.stdlib

class AllStdLibModules(outputToConsoleFunction: (String) -> Unit = { print(it) }) : MultipleLibModules(
    name = "AllStdlib",
    modules = listOf(
        CoreLibModule(),
        object : IOLibModule() {
            override fun outputToConsole(output: String) {
                outputToConsoleFunction(output)
            }
        },
        CollectionsLibModule(),
        TextLibModule(),
        RegexLibModule(),
        MathLibModule(),
        ByteLibModule(),
        RangeLibModule(),
        KDateTimeLibModule(),
    )
)
