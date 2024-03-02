package com.sunnychung.gradle.plugin.kotlite.codegenerator.domain

import com.sunnychung.gradle.plugin.kotlite.codegenerator.KotliteModuleConfig
import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeToUpperBound
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.isPrimitive

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
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanType
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.CharType
import com.sunnychung.lib.multiplatform.kotlite.model.CharValue
import com.sunnychung.lib.multiplatform.kotlite.model.DataType
import com.sunnychung.lib.multiplatform.kotlite.model.DelegatedValue
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionType
import com.sunnychung.lib.multiplatform.kotlite.model.IntType
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaValue
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.LongType
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.NothingType
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.ObjectType
import com.sunnychung.lib.multiplatform.kotlite.model.PairValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringType
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterType
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue

${config.imports.joinToString("") { "import $it\n" } }

${config.typeAliases.toList().joinToString { "typealias ${it.first} = ${it.second}\n" }}

abstract class Abstract${name}LibModule : LibraryModule("$name") {
    override val classes = emptyList<ProvidedClassDefinition>()

    override val properties = listOf<ExtensionProperty>(${extensionProperties.joinToString("") { "\n${it.generate(SourcePosition(name, 1, 1), indent(8))},\n" }}
    )
    
    override val functions = listOf<CustomFunctionDefinition>(${functionInterfaces.joinToString("") { "\n${it.generate(SourcePosition(name, 1, 1), indent(8))},\n" }}
    )
}
"""
    }

    fun FunctionDeclarationNode.generate(position: SourcePosition, indent: String): String {
        with (ScopedDelegationCodeGenerator(typeParameters)) {
            return generate(position, indent)
        }
    }

    fun PropertyDeclarationNode.generate(position: SourcePosition, indent: String): String {
        with (ScopedDelegationCodeGenerator(typeParameters)) {
            return generate(position, indent)
        }
    }
}

internal class ScopedDelegationCodeGenerator(private val typeParameterNodes: List<TypeParameterNode>) {

    val typeParameters = typeParameterNodes.associate {
        it.name to (it.typeUpperBound ?: TypeNode("Any", null, true))
    }

    fun resolve(type: TypeNode): TypeNode {
        return type.resolveGenericParameterTypeToUpperBound(typeParameterNodes)
    }

    fun FunctionDeclarationNode.generate(position: SourcePosition, indent: String): String {
        return """CustomFunctionDefinition(
    receiverType = ${receiver?.let { "\"${it.descriptiveName().escape()}\"" } ?: "null"},
    functionName = "${name.escape()}",
    returnType = "${returnType.descriptiveName()}",
    parameterTypes = listOf(${if (valueParameters.isNotEmpty()) "\n${valueParameters.joinToString("") { "${it.generate(indent(8))},\n" }}${indent(4)}" else ""}),
    typeParameters = ${if (typeParameters.isEmpty()) "emptyList()" else "listOf(${typeParameters.joinToString("") {
        "\n${it.generate(indent(8))}," }
    }\n${indent(4)})"},
    modifiers = setOf<FunctionModifier>(${modifiers.joinToString(", ") {
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
                "*${valueParameters.first().name}_.toTypedArray()"
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
        val type = resolve(_type)

        fun TypeNode.toDataTypeCode(): String {
            return if (typeParameters.containsKey(this.name)) {
                "typeArgs[\"${this.name}\"]!!.copyOf(isNullable = ${this.isNullable})"
            } else if (this.isPrimitive()) {
                "${this.name}Type(isNullable = ${this.isNullable})"
            } else {
                "ObjectType(${this.name}Value.clazz, listOf<DataType>(${this.arguments?.joinToString(", ") { it.toDataTypeCode() }}), superType = null)"
            }
        }

        val typeArgs = _type.arguments.let { typeArgs ->
            if (typeArgs.isNullOrEmpty()) {
                ""
            } else {
                ", " + typeArgs.joinToString(", ") {
                    it.toDataTypeCode()
                }
            }
        } + " /* _t = ${_type.descriptiveName()}; t = ${type.descriptiveName()} */"
        val symbolTableArg = if (!type.isPrimitive()) {
            ", symbolTable = interpreter.symbolTable()"
        } else ""
        val wrappedValue = if (type.name == "Any") {
            "it as RuntimeValue"
        } else {
            "${type.name}Value(it$typeArgs$symbolTableArg)"
        }
        val preMap = when (type.name) { // TODO change hardcoded conversions to more generic handling
            "Map", "MutableMap" -> {
                when (type.arguments?.get(1)?.name) {
                    "List" -> "?.mapValues { ListValue(it.value, ${_type.arguments!!.get(1)!!.arguments!!.get(0)!!.toDataTypeCode()}$symbolTableArg) }"
                    else -> ""
                }
            }
            "List", "Iterable" -> {
                when (type.arguments?.get(0)?.name) {
                    "Pair" -> "?.map { PairValue(it, ${_type.arguments!!.get(0)!!.arguments!!.get(0)!!.toDataTypeCode()}, ${_type.arguments!!.get(0)!!.arguments!!.get(1)!!.toDataTypeCode()}$symbolTableArg) }"
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
            else -> ""
        }
        return if (type.name != "Unit") "$variableName$preMap?.let { $wrappedValue } ?: NullValue" else "UnitValue"
    }

    // Interpreter runtime value -> kotlin value
    fun unwrapOne(variableName: String, _type: TypeNode): String {
        val type = resolve(_type)
        val question = if (type.isNullable) "?" else ""
        return if (type is FunctionTypeNode) {
            generateLambda(variableName, _type as FunctionTypeNode, 4)
        } else if (type.name == "Unit") {
            "Unit"
        } else if (type.name == "Any") {
            "$variableName as RuntimeValue"
        } else {
            if (type.isPrimitive()) {
                "($variableName as$question ${type.name}Value)$question.value"
            } else {
                "($variableName as$question DelegatedValue<*>)$question.value as ${type.name}${
                    type.arguments?.let {
                        "<${it.joinToString(", ") { "RuntimeValue" }}>"
                    } ?: ""
                }$question"
            }
        }
    }

    fun unwrap(variableName: String, type: TypeNode, isVararg: Boolean = false): String {
        return if (isVararg) {
            "($variableName as ListValue).value.map { ${unwrapOne("it", type)} }"
        } else if (type.name in setOf("List", "Iterable")) {
            // do not transform MutableList, otherwise no side effect can be performed
            val postUnwrap = if (type.name == "MutableList") ".toMutableList()" else ""
            "($variableName as DelegatedValue<${type.name}<*>>).value.map { ${unwrapOne("it", type.arguments!!.first())} }$postUnwrap"
        } else {
            unwrapOne(variableName, type)
        }
    }

    fun unwrapValueType(type: TypeNode): String {
        fun replace(type: TypeNode): TypeNode {
            // TODO don't hardcode
            if (type.name == "Any") {
                return TypeNode("RuntimeValue", null, false)
            } else if (type.name in typeParameters.keys) {
                return replace(typeParameters[type.name]!!)
            }
            return TypeNode(
                type.name,
                type.arguments?.map { replace(it) },
                type.isNullable,
                type.transformedRefName,
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
    getter = ${accessors?.getter?.let { """{ interpreter, receiver ->
        ${
            if (!receiver!!.name.endsWith(".Companion")) {
                "val unwrappedReceiver = ${unwrap("receiver", receiver!!)}"
            } else ""
        }
        val result = ${if (receiver!!.name.endsWith(".Companion")) receiver!!.name else "unwrappedReceiver"}.$name
        ${wrap("result", type)}
    }""" } ?: "null"},
    setter = ${accessors?.setter?.let { """{ interpreter, receiver, value ->
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

internal fun indent(n: Int) = " ".repeat(n)

internal fun String.escape() =
    this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
