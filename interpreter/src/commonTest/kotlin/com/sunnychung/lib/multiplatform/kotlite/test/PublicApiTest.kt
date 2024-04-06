package com.sunnychung.lib.multiplatform.kotlite.test

import com.sunnychung.lib.multiplatform.kotlite.KotliteInterpreter
import com.sunnychung.lib.multiplatform.kotlite.evalKotliteExpression
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PublicApiTest {
    @Test
    fun evalCode() {
        val interpreter = KotliteInterpreter(
            filename = "<Test>",
            code = "val a = (1 + 2) * 4",
            executionEnvironment = ExecutionEnvironment(),
        )
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        val result = symbolTable.findPropertyByDeclaredName("a")
        assertTrue(result is IntValue)
        assertEquals(12, result.value)
    }

    @Test
    fun evalExpression() {
        val result = evalKotliteExpression(
            filename = "<Test>",
            code = "(1 + 2) * 4",
            executionEnvironment = ExecutionEnvironment(),
        )
        assertTrue(result is IntValue)
        assertEquals(12, result.value)
    }
}
