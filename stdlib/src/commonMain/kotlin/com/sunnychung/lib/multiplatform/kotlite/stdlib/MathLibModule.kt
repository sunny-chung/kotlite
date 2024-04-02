package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.GlobalProperty
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import kotlin.math.E
import kotlin.math.PI

class MathLibModule : AbstractMathLibModule() {
    override val globalProperties: List<GlobalProperty> = super.globalProperties +
        listOf(
            GlobalProperty(
                position = SourcePosition("Math", 1, 1),
                declaredName = "E",
                type = "Double",
                isMutable = false,
                getter = { interpreter -> DoubleValue(E, interpreter.symbolTable()) },
            ),

            GlobalProperty(
                position = SourcePosition("Math", 1, 1),
                declaredName = "PI",
                type = "Double",
                isMutable = false,
                getter = { interpreter -> DoubleValue(PI, interpreter.symbolTable()) },
            ),
        )
}
