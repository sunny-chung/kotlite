package com.sunnychung.lib.multiplatform.kotlite.extension

import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.PrimitiveTypeName
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode

fun isValidIntegerLiteralAssignToByte(valueNode: ASTNode?, subjectType: DataType): Boolean {
    if (!(subjectType isPrimitiveTypeOf PrimitiveTypeName.Byte)) return false
    val value = when (valueNode) {
        is IntegerNode -> valueNode.value
        is UnaryOpNode -> if (valueNode.node is IntegerNode) {
            (valueNode.node as IntegerNode).value * when (valueNode.operator) {
                "+" -> 1
                "-" -> -1
                else -> throw RuntimeException("Unexpected unary operator `${valueNode.operator}`")
            }
        } else {
            return false
        }
        else -> return false
    }
    return value in Byte.MIN_VALUE .. Byte.MAX_VALUE
}
