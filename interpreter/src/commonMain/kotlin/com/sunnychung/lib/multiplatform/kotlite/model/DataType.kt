package com.sunnychung.lib.multiplatform.kotlite.model

sealed interface DataType

data object IntType : DataType
data object DoubleType : DataType
data object BooleanType : DataType
data object StringType : DataType
data object UnitType : DataType
data object NullType : DataType
data object ObjectType : DataType
