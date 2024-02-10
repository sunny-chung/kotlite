package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
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
            }
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
            }
        ),
    )

    open fun outputToConsole(output: String) {
        print(output)
    }
}
