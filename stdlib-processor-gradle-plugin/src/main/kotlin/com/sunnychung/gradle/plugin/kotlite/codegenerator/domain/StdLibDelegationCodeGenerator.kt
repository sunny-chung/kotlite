package com.sunnychung.gradle.plugin.kotlite.codegenerator.domain

import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode

internal class StdLibDelegationCodeGenerator(val name: String, val headers: String) {
    val parser = Parser(Lexer(headers))
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

package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionParameter
import com.sunnychung.lib.multiplatform.kotlite.model.CharValue
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaValue
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
//import com.sunnychung.lib.multiplatform.kotlite.model.RegexValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue

open class ${name}LibModule : LibraryModule("$name") {
    override val properties = listOf(${extensionProperties.joinToString("") { "\n${it.generate(indent(8))},\n" }}
    )
    
    override val functions = listOf(${functionInterfaces.joinToString("") { "\n${it.generate(indent(8))},\n" }}
    )
}
"""
    }

    fun FunctionDeclarationNode.generate(indent: String): String {
        return """CustomFunctionDefinition(
    receiverType = ${receiver?.let { "\"${it.escape()}\"" } ?: "null"},
    functionName = "${name.escape()}",
    returnType = "${returnType.descriptiveName()}",
    parameterTypes = listOf(${if (valueParameters.isNotEmpty()) "\n${valueParameters.joinToString("") { "${it.generate(indent(8))},\n" }}${indent(4)}" else ""}),
    executable = { receiver, args ->
        ${
            if (receiver != null) {
                val isReceiverNullable = receiver!!.endsWith('?')
                val question = if (isReceiverNullable) "?" else ""
                "val unwrappedReceiver = (receiver as$question ${receiver!!.trimEnd('?')}Value)$question.value"
            } else ""
        }
    ${valueParameters.mapIndexed { i, it ->
            "${indent(4)}val ${it.name}_ = ${unwrap("args[$i]", it.type)}\n"
        }.joinToString("")}
        val result = ${if (receiver != null) "unwrappedReceiver." else ""}$name(${valueParameters.joinToString(", ") {it.name + "_"}})
        ${wrap("result", returnType)}
    }
)""".prependIndent(indent)
    }

    // kotlin value -> Interpreter runtime value
    fun wrap(variableName: String, type: TypeNode): String {
        return if (type.name != "Unit") "$variableName?.let { ${type.name}Value(it) } ?: NullValue" else "UnitValue"
    }

    // Interpreter runtime value -> kotlin value
    fun unwrap(variableName: String, type: TypeNode): String {
        val question = if (type.isNullable) "?" else ""
        return if (type is FunctionTypeNode) {
            generateLambda(variableName, type, 4)
        } else if (type.name == "Unit") {
            "Unit"
        } else {
            "($variableName as$question ${type.name}Value)$question.value"
        }
    }

    fun generateLambda(variableName: String, type: FunctionTypeNode, indent: Int): String {
        return """{ ${type.parameterTypes!!.mapIndexed { i, it -> "arg$i: ${it.descriptiveName()}" }.joinToString(", ")} ${if (!type.parameterTypes!!.isEmpty()) "->" else ""}
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
        })"""
    }

    fun PropertyDeclarationNode.generate(indent: String): String {
        val isReceiverNullable = receiver!!.endsWith('?')
        val receiverQuestion = if (isReceiverNullable) "?" else ""

        return """ExtensionProperty(
    declaredName = "${name.escape()}",
    receiver = "${receiver!!.escape()}",
    type = "${type.descriptiveName().escape()}",
    getter = ${accessors?.getter?.let { """{ receiver ->
        val unwrappedReceiver = (receiver as$receiverQuestion ${receiver!!.trimEnd('?')}Value)$receiverQuestion.value
        val result = unwrappedReceiver.$name
        ${wrap("result", type)}
    }""" } ?: "null"},
    setter = ${accessors?.setter?.let { """{ receiver, value ->
        val unwrappedReceiver = (receiver as$receiverQuestion ${receiver!!.trimEnd('?')}Value)$receiverQuestion.value
        val unwrappedValue = ${unwrap("value", type)}
        unwrappedReceiver.$name = unwrappedValue
    }""" } ?: "null"},
)""".prependIndent(indent)
    }

    fun indent(n: Int) = " ".repeat(n)

    fun String.escape() =
        this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
}
