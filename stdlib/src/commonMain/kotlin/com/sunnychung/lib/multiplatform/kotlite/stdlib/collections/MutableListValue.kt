package com.sunnychung.lib.multiplatform.kotlite.stdlib.collections

import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

fun MutableListValue(value: MutableList<RuntimeValue>, typeArgument: DataType, symbolTable: SymbolTable)
    = DelegatedValue<MutableList<RuntimeValue>>(value, MutableListClass.clazz, listOf(typeArgument), symbolTable)

object MutableListClass {
    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "MutableList",
        typeParameters = listOf(TypeParameterNode(position = SourcePosition("Collections", 1, 1), name = "T", typeUpperBound = null)),
        isInstanceCreationAllowed = false,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        superInterfaceTypeNames = listOf("MutableCollection<T>"),
        superClassInvocationString = "List<T>()",
        position = SourcePosition("Collections", 1, 1),
    )
}
