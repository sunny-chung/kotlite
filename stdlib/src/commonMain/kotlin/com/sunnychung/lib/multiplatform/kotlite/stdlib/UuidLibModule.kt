package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.benasher44.uuid.uuid4
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.uuid.UuidClass

class UuidLibModule : AbstractUuidLibModule() {
    override val classes: List<ProvidedClassDefinition> = super.classes + listOf(
        UuidClass.clazz
    )
    override val functions: List<CustomFunctionDefinition> = super.functions + listOf(
        CustomFunctionDefinition(
            position = SourcePosition("Uuid", 1, 1),
            receiverType = null,
            functionName = "uuidString",
            returnType = "String",
            parameterTypes = emptyList(),
            executable = { interpreter, receiver, args, typeArgs ->
                val uuidString = uuid4().toString()
                StringValue(uuidString, interpreter.symbolTable())
            }
        )
    )
}
