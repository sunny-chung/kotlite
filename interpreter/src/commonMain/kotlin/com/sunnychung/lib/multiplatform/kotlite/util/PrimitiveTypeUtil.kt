package com.sunnychung.lib.multiplatform.kotlite.util

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ByteValue
import com.sunnychung.lib.multiplatform.kotlite.model.CharValue
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.PrimitiveTypeName
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

fun wrapPrimitiveValueAsRuntimeValue(value: Any?, type: DataType, symbolTable: SymbolTable) =
    when {
        type isPrimitiveTypeOf PrimitiveTypeName.Int -> IntValue(value as Int, symbolTable)
        type isPrimitiveTypeOf PrimitiveTypeName.Long -> LongValue(value as Long, symbolTable)
        type isPrimitiveTypeOf PrimitiveTypeName.Double -> DoubleValue(value as Double, symbolTable)
        type isPrimitiveTypeOf PrimitiveTypeName.Char -> CharValue(value as Char, symbolTable)
        type isPrimitiveTypeOf PrimitiveTypeName.Byte -> ByteValue(value as Byte, symbolTable)
        type isPrimitiveTypeOf PrimitiveTypeName.Boolean -> BooleanValue(value as Boolean, symbolTable)
        type isPrimitiveTypeOf PrimitiveTypeName.String -> StringValue(value as String, symbolTable)
        else -> value as RuntimeValue
    }
