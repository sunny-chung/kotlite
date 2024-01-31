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
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode

internal class StdLibDelegationCodeGenerator(val name: String, val code: String, val outputPackage: String, val config: KotliteModuleConfig) {
    val parser = Parser(Lexer(code))
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
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleType
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionType
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterModifier
import com.sunnychung.lib.multiplatform.kotlite.model.IntType
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaValue
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.LongType
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullType
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.StringType
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameter
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterType
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue
import com.sunnychung.lib.multiplatform.kotlite.model.UnitType
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue

${config.imports.joinToString("") { "import $it\n" } }

abstract class Abstract${name}LibModule : LibraryModule("$name") {
    override val classes = emptyList<ProvidedClassDefinition>()

    override val properties = listOf<ExtensionProperty>(${extensionProperties.joinToString("") { "\n${it.generate(indent(8))},\n" }}
    )
    
    override val functions = listOf<CustomFunctionDefinition>(${functionInterfaces.joinToString("") { "\n${it.generate(indent(8))},\n" }}
    )
}
"""
    }

    fun FunctionDeclarationNode.generate(indent: String): String {
        with (ScopedDelegationCodeGenerator(typeParameters)) {
            return generate(indent)
        }
    }

    fun PropertyDeclarationNode.generate(indent: String): String {
        with (ScopedDelegationCodeGenerator(typeParameters)) {
            return generate(indent)
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

    fun FunctionDeclarationNode.generate(indent: String): String {
        return """CustomFunctionDefinition(
    receiverType = ${receiver?.let { "\"${it.descriptiveName().escape()}\"" } ?: "null"},
    functionName = "${name.escape()}",
    returnType = "${returnType.descriptiveName()}",
    parameterTypes = listOf(${if (valueParameters.isNotEmpty()) "\n${valueParameters.joinToString("") { "${it.generate(indent(8))},\n" }}${indent(4)}" else ""}),
    typeParameters = ${if (typeParameters.isEmpty()) "emptyList()" else "listOf(${typeParameters.joinToString("") {
        "\n${it.generate(indent(8))}," }
    }\n${indent(4)})"},
    executable = { receiver, args, typeArgs ->
        ${
            if (receiver != null && !receiver!!.name.endsWith(".Companion")) {
                val isReceiverNullable = receiver!!.isNullable
                val question = if (isReceiverNullable) "?" else ""
                "val unwrappedReceiver = (receiver as$question ${receiver!!.name}Value)$question.value"
            } else ""
        }
    ${valueParameters.mapIndexed { i, it ->
            "${indent(4)}val ${it.name}_ = ${unwrap("args[$i]", it.type)}\n"
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
                "*(args[0] as ListValue).value.toTypedArray()"
            } else {
                valueParameters.joinToString(", ") { it.name + "_" }
            }
        })
        ${wrap("result", returnType)}
    }
)""".prependIndent(indent)
    }

    // kotlin value -> Interpreter runtime value
    fun wrap(variableName: String, _type: TypeNode): String {
        val type = resolve(_type)
        val typeArgs = if (_type.arguments.isNullOrEmpty()) {
            ""
        } else {
            _type.arguments!!.joinToString("") {
                ", " + if (typeParameters.containsKey(it.name)) {
                    "typeArgs[\"${it.name}\"]!!.copyOf(isNullable = ${it.isNullable})"
                } else {
                    "${it.name}Type(isNullable = ${it.isNullable})"
                }
            }
        }
        val wrappedValue = if (type.name == "Any") {
            "it as RuntimeValue"
        } else {
            "${type.name}Value(it$typeArgs)"
        }
        return if (type.name != "Unit") "$variableName?.let { $wrappedValue } ?: NullValue" else "UnitValue"
    }

    // Interpreter runtime value -> kotlin value
    fun unwrap(variableName: String, type: TypeNode): String {
        val type = resolve(type)
        val question = if (type.isNullable) "?" else ""
        return if (type is FunctionTypeNode) {
            generateLambda(variableName, type, 4)
        } else if (type.name == "Unit") {
            "Unit"
        } else if (type.name == "Any") {
            "$variableName as RuntimeValue"
        } else {
            "($variableName as$question ${type.name}Value)$question.value"
        }
    }

    fun unwrapValueType(type: TypeNode): String {
        fun replace(type: TypeNode): TypeNode {
            // TODO don't hardcode
            if (type.name == "Any") {
                return TypeNode("RuntimeValue", null, false)
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
                ", modifiers = setOf(${modifiers.joinToString(", ") { "FunctionValueParameterModifier.$it" }})"
            } else ""
        })"""
    }

    fun TypeParameterNode.generate(indent: String): String {
        return """${indent}TypeParameter(name = "$name", typeUpperBound = ${typeUpperBound?.let { "\"${it.descriptiveName()}\"" } ?: "null"})"""
    }

    fun PropertyDeclarationNode.generate(indent: String): String {
        val isReceiverNullable = receiver!!.isNullable
        val receiverQuestion = if (isReceiverNullable) "?" else ""

        return """ExtensionProperty(
    declaredName = "${name.escape()}",
    typeParameters = listOf<TypeParameter>(${typeParameters.joinToString("") {
        "\n${it.generate(indent(8))}," }
    }${indent(4)}),
    receiver = "${receiver!!.descriptiveName().escape()}",
    type = "${type.descriptiveName().escape()}",
    getter = ${accessors?.getter?.let { """{ receiver ->
        ${
            if (!receiver!!.name.endsWith(".Companion")) {
                "val unwrappedReceiver = (receiver as$receiverQuestion ${receiver!!.name}Value)$receiverQuestion.value"
            } else ""
        }
        val result = ${if (receiver!!.name.endsWith(".Companion")) receiver!!.name else "unwrappedReceiver"}.$name
        ${wrap("result", type)}
    }""" } ?: "null"},
    setter = ${accessors?.setter?.let { """{ receiver, value ->
        ${
            if (!receiver!!.name.endsWith(".Companion")) {
                "val unwrappedReceiver = (receiver as$receiverQuestion ${receiver!!.name}Value)$receiverQuestion.value"
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
