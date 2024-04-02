package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.GlobalProperty
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import kotlin.test.Test

class CustomBuiltinPropertyTest {

    @Test
    fun cannotWriteToImmutableProperty() {
        val env = ExecutionEnvironment().apply {
            registerGlobalProperty(
                GlobalProperty(
                    position = SourcePosition("<Test>", 1, 1),
                    declaredName = "myPi",
                    type = "Double",
                    isMutable = false,
                    getter = { interpreter -> DoubleValue(3.14, interpreter.symbolTable()) },
                )
            )
        }
        assertSemanticFail("""
            myPi = 123.0
        """.trimIndent(), environment = env)
    }

    @Test
    fun incorrectType() {
        val env = ExecutionEnvironment().apply {
            registerGlobalProperty(
                GlobalProperty(
                    position = SourcePosition("<Test>", 1, 1),
                    declaredName = "myPi",
                    type = "Double",
                    isMutable = false,
                    getter = { interpreter -> DoubleValue(3.14, interpreter.symbolTable()) },
                )
            )
        }
        assertSemanticFail("""
            val x: Int? = myPi
        """.trimIndent(), environment = env)
    }
}
