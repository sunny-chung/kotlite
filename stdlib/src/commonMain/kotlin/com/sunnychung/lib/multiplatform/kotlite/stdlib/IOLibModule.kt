package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue

open class IOLibModule : LibraryModule("IO") {
    override val classes: List<ProvidedClassDefinition> = emptyList()

    override val properties: List<ExtensionProperty> = emptyList()

    override val functions: List<CustomFunctionDefinition> = listOf(
        CustomFunctionDefinition(
            receiverType = null,
            functionName = "println",
            returnType = "Unit",
            parameterTypes = listOf(CustomFunctionParameter("message", "Any?")),
            executable = { interpreter, receiver, args, typeArgs ->
                val message = args[0]
                outputToConsole("${message.convertToString()}\n")
                UnitValue
            },
            position = SourcePosition(name, 1, 1),
        ),
        CustomFunctionDefinition(
            receiverType = null,
            functionName = "print",
            returnType = "Unit",
            parameterTypes = listOf(CustomFunctionParameter("message", "Any?")),
            executable = { interpreter, receiver, args, typeArgs ->
                val message = args[0]
                outputToConsole(message.convertToString())
                UnitValue
            },
            position = SourcePosition(name, 2, 1),
        ),
    )

    open fun outputToConsole(output: String) {
        print(output)
    }
}
