package com.sunnychung.gradle.plugin.kotlite.codegenerator.domain

import com.sunnychung.gradle.plugin.kotlite.codegenerator.KotliteModuleConfig
import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeToUpperBound
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.isPrimitive
import com.sunnychung.lib.multiplatform.kotlite.model.isPrimitiveWithValue

internal val isDebug: Boolean = false

internal class StdLibDelegationCodeGenerator(val name: String, val code: String, val outputPackage: String, val config: KotliteModuleConfig) {
    val parser = Parser(Lexer(name, code))
    val extensionProperties: List<PropertyDeclarationNode>
    val functionInterfaces: List<FunctionDeclarationNode>

    init {
        val parsed = parser.libHeaderFile()
        extensionProperties = parsed.filterIsInstance<PropertyDeclarationNode>()
        functionInterfaces = parsed.filterIsInstance<FunctionDeclarationNode>()
    }

    init {
        if (name.any { !it.isJavaIdentifierPart() }) {
            throw IllegalArgumentException("Provided name should be a valid String that can be included as a part of a Kotlin identifier")
        }
    }

    fun generate(): String {
        return """
/** Generated code. DO NOT MODIFY! Changes to this file will be overwritten. **/

package $outputPackage

import com.sunnychung.lib.multiplatform.kotlite.model.AnyType
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ByteValue
import com.sunnychung.lib.multiplatform.kotlite.model.ComparableRuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.CharValue
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionType
import com.sunnychung.lib.multiplatform.kotlite.model.GlobalProperty
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.IterableValue
import com.sunnychung.lib.multiplatform.kotlite.model.IteratorClass
import com.sunnychung.lib.multiplatform.kotlite.model.IteratorValue
import com.sunnychung.lib.multiplatform.kotlite.model.KotlinValueHolder
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaValue
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.ListClass
import com.sunnychung.lib.multiplatform.kotlite.model.ListValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.NothingType
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.ObjectType
import com.sunnychung.lib.multiplatform.kotlite.model.PairClass
import com.sunnychung.lib.multiplatform.kotlite.model.PairValue
import com.sunnychung.lib.multiplatform.kotlite.model.PrimitiveIterableValue
import com.sunnychung.lib.multiplatform.kotlite.model.PrimitiveIteratorValue
import com.sunnychung.lib.multiplatform.kotlite.model.PrimitiveType
import com.sunnychung.lib.multiplatform.kotlite.model.PrimitiveTypeName
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterType
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue
import com.sunnychung.lib.multiplatform.kotlite.util.wrapPrimitiveValueAsRuntimeValue

${config.imports.joinToString("") { "import $it\n" } }

${config.typeAliases.toList().joinToString { "typealias ${it.first} = ${it.second}\n" }}

abstract class Abstract${name}LibModule : LibraryModule("$name") {
    override val classes = emptyList<ProvidedClassDefinition>()

    override val properties = listOf<ExtensionProperty>(${extensionProperties.joinToString("") { "\n${it.generate(SourcePosition(name, it.position.lineNum, it.position.col), indent(8))},\n" }}
    )
    
    override val globalProperties = emptyList<GlobalProperty>()
    
    override val functions = listOf<CustomFunctionDefinition>(${functionInterfaces.joinToString("") { "\n${it.generate(SourcePosition(name, it.position.lineNum, it.position.col), indent(8))},\n" }}
    )
}
"""
    }

    fun FunctionDeclarationNode.generate(position: SourcePosition, indent: String): String {
        with (ScopedDelegationCodeGenerator(
            typeParameterNodes = typeParameters,
            isNullAware = modifiers.contains(FunctionModifier.nullaware),
        )) {
            return generate(position, indent)
        }
    }

    fun PropertyDeclarationNode.generate(position: SourcePosition, indent: String): String {
        with (ScopedDelegationCodeGenerator(typeParameterNodes = typeParameters, isNullAware = false)) {
            return generate(position, indent)
        }
    }
}

internal class ScopedDelegationCodeGenerator(private val typeParameterNodes: List<TypeParameterNode>, private val isNullAware: Boolean) {

    val typeParameters = typeParameterNodes.associate {
        it.name to (it.typeUpperBound ?: TypeNode(SourcePosition.NONE, "Any", null, true))
    }

    val typeParametersUsedIn = mutableMapOf<String, String>()

//    private fun resolveTypeParameterNodeRetainTypeParameters(node: TypeNode): TypeNode {
//        if (typeParameters.containsKey(node.name) && node.arguments == null) {
//            return TypeNode(node.position, node.name, TypeNode(node.position, node.name, null, false))
//        }
//        return TypeNode(node.position, node.name, node.arguments?.map { resolveTypeParameterNodeRetainTypeParameters(it) }, node.isNullable)
//    }

    val typeParameterNodesRetainTypeParameters = typeParameterNodes.map { TypeParameterNode(
        it.position, it.name, if (it.typeUpperBound == null && typeParameters.containsKey(it.name)) {
            TypeNode(SourcePosition.NONE, it.name, null, false)
        } else {
//            it.typeUpperBound?.let { resolveTypeParameterNodeRetainTypeParameters(it) }
            it.typeUpperBound
        }
    ) }

    fun resolve(type: TypeNode): TypeNode {
        return type.resolveGenericParameterTypeToUpperBound(typeParameterNodes)
    }

    fun resolveForWrap(type: TypeNode): TypeNode {
        return type.resolveGenericParameterTypeToUpperBound(typeParameterNodes, isKeepTypeParameter = true)
    }

    fun FunctionDeclarationNode.generate(position: SourcePosition, indent: String): String {
        if (receiver != null && receiver!!.name in this@ScopedDelegationCodeGenerator.typeParameters) {
            // receiver itself is a type parameter
            typeParametersUsedIn[receiver!!.name] = "receiver"
        }
        valueParameters.forEachIndexed { index, it ->
            if (it.type.name in this@ScopedDelegationCodeGenerator.typeParameters) {
                typeParametersUsedIn[it.type.name] = "args[$index]"
            }
        }

        return """CustomFunctionDefinition(
    receiverType = ${receiver?.let { "\"${it.descriptiveName().escape()}\"" } ?: "null"},
    functionName = "${name.escape()}",
    returnType = "${returnType.descriptiveName()}",
    parameterTypes = listOf(${if (valueParameters.isNotEmpty()) "\n${valueParameters.joinToString("") { "${it.generate(indent(8))},\n" }}${indent(4)}" else ""}),
    typeParameters = ${if (typeParameters.isEmpty()) "emptyList()" else "listOf(${typeParameters.joinToString("") {
        "\n${it.generate(indent(8))}," }
    }\n${indent(4)})"},
    modifiers = setOf<FunctionModifier>(${modifiers.filter { it != FunctionModifier.nullaware }.joinToString(", ") {
        "FunctionModifier.${it.name}" }
    }),
    executable = { interpreter, receiver, args, typeArgs ->
        ${
            if (receiver != null && !receiver!!.name.endsWith(".Companion")) {
                val isReceiverNullable = receiver!!.isNullable
                val question = if (isReceiverNullable) "?" else ""
                "val unwrappedReceiver = ${unwrap("receiver", receiver!!)}"
            } else ""
        }
    ${valueParameters.mapIndexed { i, it ->
            "${indent(4)}val ${it.name}_ = ${unwrap("args[$i]", it.type, it.modifiers.contains(FunctionValueParameterModifier.vararg))}\n"
        }.joinToString("")}
        val result = ${
            if (receiver != null) {
                if (receiver!!.name.endsWith(".Companion")) {
                    "${receiver!!.name}."
                } else {
                    "unwrappedReceiver."
                }
            } else ""
        }$name(${
            if (valueParameters.size == 1 && valueParameters.first().modifiers.contains(FunctionValueParameterModifier.vararg)) {
                "*${valueParameters.first().name}_.${
                    if (valueParameters.first().type.name == "Byte") {
                        "toByteArray()"
                    } else {
                        "toTypedArray()"
                    }
                }"
            } else {
                valueParameters.joinToString(", ") { it.name + "_" }
            }
        })
        ${wrap("result", returnType)}
    },
    position = SourcePosition(filename = "${position.filename}", lineNum = ${position.lineNum}, col = ${position.col}),
)""".prependIndent(indent)
    }

    // kotlin value -> Interpreter runtime value
    fun wrap(variableName: String, _type: TypeNode): String {
        val isTypeATypeParameter = typeParametersUsedIn.containsKey(_type.name)
        val type = resolveForWrap(_type)

        fun TypeNode.toDataTypeCode(): String {
            return if (typeParameters.containsKey(this.name)) {
                "typeArgs[\"${this.name}\"]!!.copyOf(isNullable = ${this.isNullable})"
            } else if (this.name == "Comparable") {
                val arg = this.arguments!!.first()
                arg.toDataTypeCode()
            } else if (this.isPrimitiveWithValue()) {
                "interpreter.symbolTable().${if (this.isNullable) "Nullable" else ""}${this.name}Type"
            } else if (this.isPrimitive()) {
                "${this.name}Type(isNullable = ${this.isNullable})"
            } else {
                "ObjectType(${this.name}Class.clazz, listOf<DataType>(${this.arguments?.joinToString(", ") { it.toDataTypeCode() } ?: ""}), superTypes = emptyList())"
            }
        }

        val typeArgs = { hasCommaPrefix: Boolean ->
            val type = if (isTypeATypeParameter) {
                type
            } else {
                _type
            }
            type.arguments.let { typeArgs ->
                if (typeArgs.isNullOrEmpty()) {
                    ""
                } else {
                    (if (hasCommaPrefix) ", " else "") + typeArgs.joinToString(", ") {
                        it.toDataTypeCode()
                    }
                }
            } + " /* _t = ${_type.descriptiveName()}; t.name = ${type.name}; t = ${type.descriptiveName()} */"
        }
        val symbolTableArg = if (type.isPrimitiveWithValue() || !type.isPrimitive()) {
            ", symbolTable = interpreter.symbolTable()"
        } else ""
        val translatedTypeName = when (type.name) {
            "Collection" -> "ListValue"
            else -> "${type.name}Value"
        }
        val wrappedValue = if (type.name == "Any" || (typeParameters.containsKey(type.name) && typeParameters[type.name]!!.name == "Any")) {
            "it as RuntimeValue"
        } else if (type.name == "Nothing") {
            "it"
        } else if (isTypeATypeParameter) {
            "DelegatedValue<${type.name}${if (type.arguments?.isNotEmpty() == true) "<${type.arguments!!.joinToString(", ") { "RuntimeValue" }}>" else ""}>(it, (${typeParametersUsedIn[_type.name]}.type() as ObjectType).clazz, listOf<DataType>(${typeArgs(false)})$symbolTableArg)"
        } else {
            "$translatedTypeName(it${typeArgs(true)}$symbolTableArg)"
        }
        val preMap = when (type.name) { // TODO change hardcoded conversions to more generic handling
            "Map", "MutableMap" -> {
                when (type.arguments?.get(1)?.name) {
                    "List" -> "?.mapValues { ListValue(it.value, ${_type.arguments!!.get(1)!!.arguments!!.get(0)!!.toDataTypeCode()}$symbolTableArg) }"
                    else -> ""
                }
            }
            "MapEntry" -> {
                if (isNullAware) {
                    "?.toRuntimeValue()"
                } else {
                    ""
                }
            }
            "List", "MutableList", "Set", "MutableSet", "Iterable" -> {
                (if (isNullAware) {
                    "?.map { it.toRuntimeValue() }"
                } else "") +
                when (val subtypeName = type.arguments?.get(0)?.name) {
                    "Pair" -> "?.map { PairValue(it, ${_type.arguments!!.get(0)!!.arguments!!.get(0)!!.toDataTypeCode()}, ${_type.arguments!!.get(0)!!.arguments!!.get(1)!!.toDataTypeCode()}$symbolTableArg) }"
//                    "String" -> "?.map { StringValue(it$symbolTableArg) }"
//                    "KDateTimeFormat" -> "?.map { KDateTimeFormatValue(it$symbolTableArg) }"
//                    else -> ""
                    "Any" -> ""
                    else -> if (!typeParameters.containsKey(subtypeName) && subtypeName !in setOf("Comparable")) {
                        "?.map { ${subtypeName}Value(it$symbolTableArg) }"
                    } else {
                        ""
                    }
                } + when (type.name) {
                    "List" -> ""
                    "MutableList" -> "?.toMutableList()"
                    "Set" -> "?.toSet()"
                    "MutableSet" -> "?.toMutableSet()"
                    else -> ""
                }
            }
            "Pair" -> {
                ".let { " +
                        when (type.arguments?.get(0)?.name) {
                            "List" -> "ListValue(it.first, ${_type.arguments!!.get(0)!!.arguments!!.get(0)!!.toDataTypeCode()}$symbolTableArg)"
                            else -> "it.first"
                        } +
                        " to " +
                        when (type.arguments?.get(1)?.name) {
                            "List" -> "ListValue(it.second, ${_type.arguments!!.get(1)!!.arguments!!.get(0)!!.toDataTypeCode()}$symbolTableArg)"
                            else -> "it.second"
                        } +
                        " }"
            }
            "Iterator" -> {
                debug { "Iterator wrap -> ${_type.descriptiveName()}" }
                when (_type.arguments!!.get(0)!!.name) {
                    "MapEntry" -> ".let { it.wrap(${_type.arguments!!.get(0)!!.arguments!!.get(0)!!.toDataTypeCode()}, ${_type.arguments!!.get(0)!!.arguments!!.get(1)!!.toDataTypeCode()}$symbolTableArg) }"
                    "Byte" -> ".let { it.wrap(${symbolTableArg.removePrefix(", ")}) }"
                    else -> ""
                }
            }
            "Collection" -> {
                ".toList()"
            }
            else -> ""
        }
        return when (type.name) {
            "Unit" -> "UnitValue"
            "Comparable" -> "$variableName ?: NullValue"
            else -> "$variableName$preMap?.let { $wrappedValue } ?: NullValue"
        }
    }

    // Interpreter runtime value -> kotlin value
    fun unwrapOne(variableName: String, _type: TypeNode): String {
        val type = resolve(_type)
        val question = if (type.isNullable) "?" else ""
        val unwrapNullable = if (type.isNullable) {
            ".toNullable()"
        } else {
            ""
        }
        return if (type is FunctionTypeNode) {
            generateLambda(variableName, _type as FunctionTypeNode, 4)
        } else if (type.name == "Unit") {
            "Unit"
        } else if (type.name == "Any") {
            "$variableName as RuntimeValue".let {
                if (isNullAware && type.isNullable) {
                    "($it)$unwrapNullable"
                } else it
            }
        } else if (type.name == "Comparable") {
            "$variableName as ComparableRuntimeValue<Comparable<Any>, Any>".let {
                if (isNullAware && type.isNullable) {
                    "($it)$unwrapNullable"
                } else it
            }
        } else {
            if (type.isPrimitive()) {
                "($variableName as$question ${type.name}Value)$question.value"
            } else {
                val postMap = when (type.name) {
//                    "Comparable" -> "$question.let { makeComparable(it as Comparable<Any>) }"
                    else -> " as ${type.name}${
                        type.arguments?.let {
                            "<${it.joinToString(", ") {
                                if (it.name == "Comparable") {
                                    "ComparableRuntimeValue<Comparable<Any>, Any>"
                                } else if (it.isPrimitiveWithValue()) {
                                    "${it.name}Value"
                                } else {
                                    "RuntimeValue"
                                } +
                                    if (isNullAware && it.isNullable) {
                                        "?"
                                    } else {
                                        ""
                                    }
                            }}>"
                        } ?: ""
                    }$question"
                }

                "($variableName as$question KotlinValueHolder<*>)$question.value$postMap"
            }
        }
    }

    fun unwrap(variableName: String, type: TypeNode, isVararg: Boolean = false): String {
        return if (isVararg) {
            "($variableName as KotlinValueHolder<List<RuntimeValue>>).value.map { ${unwrapOne("it", type)} }"
        } else if (type.name in setOf("List", "Iterable", "Collection", "PrimitiveIterable")) {
            // do not transform MutableList, otherwise no side effect can be performed
            val postUnwrap = if (type.name == "MutableList") ".toMutableList()" else ""
            val preMap = if (type.name == "PrimitiveIterable") ".map { wrapPrimitiveValueAsRuntimeValue(it, typeArgs[\"T\"]!!, interpreter.symbolTable()) }" else ""
            val translatedTypeName = when (type.name) {
                "PrimitiveIterable" -> "Iterable"
                else -> type.name
            }
            "($variableName as KotlinValueHolder<$translatedTypeName<*>>).value$preMap.map { ${unwrapOne("it", type.arguments!!.first())} }$postUnwrap"
        } else {
            unwrapOne(variableName, type)
        }
    }

    fun unwrapValueType(type: TypeNode): String {
        fun replace(type: TypeNode): TypeNode {
            // TODO don't hardcode
            if (type.name == "Any") {
                return TypeNode(SourcePosition.NONE, "RuntimeValue", null, isNullAware && type.isNullable)
            } else if (type.name in typeParameters.keys) {
                return replace(typeParameters[type.name]!!)
            }
            return TypeNode(
                position = SourcePosition.NONE,
                name = type.name,
                arguments = type.arguments?.map { replace(it) },
                isNullable = type.isNullable,
                transformedRefName = type.transformedRefName,
            )
        }
        return replace(type).descriptiveName()
    }

    fun generateLambda(variableName: String, type: FunctionTypeNode, indent: Int): String {
        return """{ ${type.parameterTypes!!.mapIndexed { i, it -> "arg$i: ${unwrapValueType(it)}" }.joinToString(", ")} ${if (!type.parameterTypes!!.isEmpty()) "->" else ""}
${type.parameterTypes!!.mapIndexed { i, it -> "        val wa$i = ${wrap("arg$i", it)}" }.joinToString("\n")}

        val result = ($variableName as LambdaValue).execute(arrayOf(${type.parameterTypes!!.indices.joinToString(", ") { "wa$it" }}))
        ${unwrap("result", type.returnType!!)}
    }""".prependIndent(indent(indent)).trimStart()
    }

    fun FunctionValueParameterNode.generate(indent: String): String {
        return """${indent}CustomFunctionParameter(name = "$name", type = "${type.descriptiveName()}"${
            if (defaultValue != null) {
                ", defaultValueExpression = \"${CodeGenerator(defaultValue!!, isPrintDebugInfo = false).generateCode().escape()}\""
            } else ""
        }${
            if (modifiers.isNotEmpty()) {
                ", modifiers = setOf(${modifiers.joinToString(", ") { "\"$it\"" }})"
            } else ""
        })"""
    }

    fun TypeParameterNode.generate(indent: String): String {
        return """${indent}TypeParameter(name = "$name", typeUpperBound = ${typeUpperBound?.let { "\"${it.descriptiveName()}\"" } ?: "null"})"""
    }

    fun PropertyDeclarationNode.generate(position: SourcePosition, indent: String): String {
        val isReceiverNullable = receiver!!.isNullable
        val receiverQuestion = if (isReceiverNullable) "?" else ""

        return """ExtensionProperty(
    declaredName = "${name.escape()}",
    typeParameters = listOf<TypeParameter>(${typeParameters.joinToString("") {
        "\n${it.generate(indent(8))},"
    }}
    ${indent(4)}),
    receiver = "${receiver!!.descriptiveName().escape()}",
    type = "${type.descriptiveName().escape()}",
    getter = ${accessors?.getter?.let { """{ interpreter, receiver, typeArgs ->
        ${
            if (!receiver!!.name.endsWith(".Companion")) {
                "val unwrappedReceiver = ${unwrap("receiver", receiver!!)}"
            } else ""
        }
        val result = ${if (receiver!!.name.endsWith(".Companion")) receiver!!.name else "unwrappedReceiver"}.$name
        ${wrap("result", type)}
    }""" } ?: "null"},
    setter = ${accessors?.setter?.let { """{ interpreter, receiver, value, typeArgs ->
        ${
            if (!receiver!!.name.endsWith(".Companion")) {
                "val unwrappedReceiver = ${unwrap("receiver", receiver!!)}"
            } else ""
        }
        val unwrappedValue = ${unwrap("value", type)}
        ${if (receiver!!.name.endsWith(".Companion")) receiver!!.name else "unwrappedReceiver"}.$name = unwrappedValue
    }""" } ?: "null"},
)""".prependIndent(indent)
    }
}

internal fun debug(message: () -> String) {
    if (isDebug) {
        println(message())
    }
}

internal fun indent(n: Int) = " ".repeat(n)

internal fun String.escape() =
    this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
