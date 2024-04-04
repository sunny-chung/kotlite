package com.sunnychung.lib.multiplatform.kotlite.stdlib.range

import com.sunnychung.lib.multiplatform.kotlite.model.ComparableRuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

fun ClosedRangeValue(value: ClosedRange<ComparableRuntimeValue<Comparable<Any>, Any>>, valueType: DataType, symbolTable: SymbolTable)
    = DelegatedValue(value, ClosedRangeClass.clazz, listOf(valueType), symbolTable)

object ClosedRangeClass {
    private val position = SourcePosition("Range", 1, 1)

    val clazz = ProvidedClassDefinition(
        fullQualifiedName = "ClosedRange",
        typeParameters = listOf(
            TypeParameterNode(
                position = position,
                name = "T",
                typeUpperBound = TypeNode(
                    position = position,
                    name = "Comparable",
                    arguments = listOf(TypeNode(
                        position = position,
                        name = "T",
                        arguments = null,
                        isNullable = false,
                    )),
                    isNullable = false,
                )
            ),
        ),
        isInstanceCreationAllowed = false,
        isInterface = true,
        primaryConstructorParameters = emptyList(),
        constructInstance = { _, _, _ -> throw UnsupportedOperationException() },
        position = SourcePosition("Collections", 1, 1),
    )
}
