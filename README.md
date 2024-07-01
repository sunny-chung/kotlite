# Kotlite & Kotlite Interpreter

_A lite embedded Kotlin interpreter_

![Android](https://img.shields.io/badge/Android-blue)
![JVM](https://img.shields.io/badge/JVM-blue)
![js](https://img.shields.io/badge/js-blue)
![iOS](https://img.shields.io/badge/iOS-blue)
![macOS](https://img.shields.io/badge/macOS-blue)
![watchOS](https://img.shields.io/badge/watchOS-blue)
![tvOS](https://img.shields.io/badge/tvOS-blue)
![Verification Test Status](https://github.com/sunny-chung/kotlite/actions/workflows/run-test.yaml/badge.svg?branch=main)

**Kotlite** is an open-sourced type-safe programming language that has a rich subset of the [script](https://kotlinlang.org/docs/custom-script-deps-tutorial.html) variant of the [Kotlin](https://kotlinlang.org/) programming language. It comes with standard libraries, which are a subset of Kotlin Multiplatform/Common standard libraries and a few third-party libraries.

**Kotlite Interpreter** is a lightweight Kotlin Multiplatform library to interpret and execute codes written in Kotlite, and bridge the host runtime environment and the embedded runtime environment.

Kotlite Interpreter ![Kotlite Interpreter](https://img.shields.io/maven-central/v/io.github.sunny-chung/kotlite-interpreter)

Kotlite Stdlib ![Kotlite Stdlib](https://img.shields.io/maven-central/v/io.github.sunny-chung/kotlite-stdlib)

Kotlite Library Preprocessor ![Kotlite Library Preprocessor](https://img.shields.io/maven-central/v/io.github.sunny-chung/kotlite-stdlib-processor-plugin)

[Release Notes](CHANGELOG.md)

## TL;DR

```kotlin
val env = ExecutionEnvironment().apply {
    install(AllStdLibModules())
}
val interpreter = KotliteInterpreter(
    filename = "UserScript",
    code = """
        fun fib(n: Int): Long {
            if (n > 100) throw Exception("n is too large")
            
            val dp = mutableMapOf<Int, Long>()
            fun f(i: Int): Long {
                if (i < 0) throw Exception("Invalid i: ${'$'}i")
                if (i <= 1) return i.toLong()
                if (i in dp) {
                    return dp[i]!!
                }
                return (f(i - 2) + f(i - 1)).also {
                    dp[i] = it
                }
            }
            
            return f(n)
        }
        val a = fib(19)
    """.trimIndent(),
    executionEnvironment = env,
)
interpreter.eval()
val symbolTable = interpreter.symbolTable()
val a = (symbolTable.findPropertyByDeclaredName("a") as LongValue).value // 4181L
```

The interpreter is well tested.

![Well tested](./doc/usermanual/media/tests.png)

## Demo

[Web - Kotlite Interpreter in browser](https://sunny-chung.github.io/kotlite/demo/)

https://github.com/sunny-chung/kotlite/assets/14835950/464ddb68-5623-463e-89cb-5b34a4bebf41

https://github.com/sunny-chung/kotlite/assets/14835950/417143fa-6240-40cf-ba5f-8e40e97a4243

https://github.com/sunny-chung/kotlite/assets/14835950/f731c9a0-d941-4cd3-a23c-bfc2a60429f4

## Documentation

[Documentation site](https://sunny-chung.github.io/kotlite/)
