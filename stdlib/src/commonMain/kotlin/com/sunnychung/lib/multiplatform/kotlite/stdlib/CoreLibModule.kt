package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaValue
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter

class CoreLibModule : LibraryModule("Core") {
    override val classes: List<ProvidedClassDefinition> = emptyList()

    override val properties: List<ExtensionProperty> = emptyList()

    override val functions: List<CustomFunctionDefinition> = listOf(
        CustomFunctionDefinition(
            receiverType = "T",
            functionName = "also",
            returnType = "T",
            typeParameters = listOf(TypeParameter(name = "T", typeUpperBound = null)),
            parameterTypes = listOf(CustomFunctionParameter("block", "(T) -> Unit")),
            executable = { interpreter, receiver, args, typeArgs ->
                val block = args[0] as LambdaValue
                block.execute(arrayOf(receiver))
                receiver!!
            },
            position = SourcePosition(name, 1, 1),
        ),
        CustomFunctionDefinition(
            receiverType = "T",
            functionName = "apply",
            returnType = "T",
            typeParameters = listOf(TypeParameter(name = "T", typeUpperBound = null)),
            parameterTypes = listOf(CustomFunctionParameter("block", "T.() -> Unit")),
            executable = { interpreter, receiver, args, typeArgs ->
                val block = args[0] as LambdaValue
                block.execute(arguments = emptyArray(), receiver = receiver!!)
                receiver!!
            },
            position = SourcePosition(name, 1, 1),
        ),
        CustomFunctionDefinition(
            receiverType = "T",
            functionName = "let",
            returnType = "R",
            typeParameters = listOf(
                TypeParameter(name = "T", typeUpperBound = null),
                TypeParameter(name = "R", typeUpperBound = null),
            ),
            parameterTypes = listOf(CustomFunctionParameter("block", "(T) -> R")),
            executable = { interpreter, receiver, args, typeArgs ->
                val block = args[0] as LambdaValue
                block.execute(arrayOf(receiver))
            },
            position = SourcePosition(name, 1, 1),
        ),
        CustomFunctionDefinition(
            receiverType = "T",
            functionName = "run",
            returnType = "R",
            typeParameters = listOf(
                TypeParameter(name = "T", typeUpperBound = null),
                TypeParameter(name = "R", typeUpperBound = null),
            ),
            parameterTypes = listOf(CustomFunctionParameter("block", "T.() -> R")),
            executable = { interpreter, receiver, args, typeArgs ->
                val block = args[0] as LambdaValue
                block.execute(arguments = emptyArray(), receiver = receiver!!)
            },
            position = SourcePosition(name, 1, 1),
        ),
        CustomFunctionDefinition(
            receiverType = null,
            functionName = "run",
            returnType = "R",
            typeParameters = listOf(
                TypeParameter(name = "R", typeUpperBound = null),
            ),
            parameterTypes = listOf(CustomFunctionParameter("block", "() -> R")),
            executable = { interpreter, receiver, args, typeArgs ->
                val block = args[0] as LambdaValue
                block.execute(arguments = emptyArray())
            },
            position = SourcePosition(name, 1, 1),
        ),
        CustomFunctionDefinition(
            receiverType = null,
            functionName = "with",
            returnType = "R",
            typeParameters = listOf(
                TypeParameter(name = "T", typeUpperBound = null),
                TypeParameter(name = "R", typeUpperBound = null),
            ),
            parameterTypes = listOf(
                CustomFunctionParameter("receiver", "T"),
                CustomFunctionParameter("block", "T.() -> R"),
            ),
            executable = { interpreter, _, args, typeArgs ->
                val receiver = args[0]
                val block = args[1] as LambdaValue
                block.execute(arguments = emptyArray(), receiver = receiver)
            },
            position = SourcePosition(name, 1, 1),
        ),
    )

}
