package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter

sealed interface RuntimeValue {
    fun type(): DataType

    fun convertToString(): String
}
//data class RuntimeValue(val type: DataType, val value: Any?)

data object UnitValue : RuntimeValue {
    override fun type() = UnitType()

    override fun convertToString() = "Unit"
}
data object NullValue : RuntimeValue {
    override fun type() = NothingType(isNullable = true)

    override fun convertToString() = "null"
}

class BooleanValue(val value: Boolean, symbolTable: SymbolTable) : PrimitiveValue(symbolTable) {

    override fun primitiveType(rootSymbolTable: SymbolTable) = rootSymbolTable.BooleanType
    override fun convertToString() = value.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BooleanValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class LongValue(override val value: Long, symbolTable: SymbolTable) : NumberValue<Long>, PrimitiveValue(symbolTable) {

    override fun primitiveType(rootSymbolTable: SymbolTable) = rootSymbolTable.LongType
    override fun convertToString() = value.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class StringValue(override val value: String, symbolTable: SymbolTable) : ComparableRuntimeValueHolder<String>, PrimitiveValue(symbolTable) {

    override fun primitiveType(rootSymbolTable: SymbolTable) = rootSymbolTable.StringType
    override fun convertToString() = value
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StringValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class CharValue(override val value: Char, symbolTable: SymbolTable) : ComparableRuntimeValueHolder<Char>, PrimitiveValue(symbolTable) {

    override fun primitiveType(rootSymbolTable: SymbolTable) = rootSymbolTable.CharType
    override fun convertToString() = value.toString()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CharValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class LambdaValue(val value: LambdaLiteralNode, private val resolvedType: FunctionType, val symbolRefs: SymbolTable, private val interpreter: Interpreter) : RuntimeValue {
    override fun type() = resolvedType

    override fun convertToString() = "Lambda()"

    fun execute(arguments: Array<RuntimeValue?>): RuntimeValue {
        return interpreter.evalFunctionCall(arguments, emptyArray(), SourcePosition("TODO", 1, 1), value, emptyMap(), emptyList()).result
    }
}

internal fun findType(typeName: String, value1: RuntimeValue, value2: RuntimeValue): ObjectType {
    val type = if (value1.type().name == typeName) {
        value1.type()
    } else if (value2.type().name == typeName) {
        value2.type()
    } else {
        throw RuntimeException("Type $typeName not found")
    }
    return type as ObjectType
}
