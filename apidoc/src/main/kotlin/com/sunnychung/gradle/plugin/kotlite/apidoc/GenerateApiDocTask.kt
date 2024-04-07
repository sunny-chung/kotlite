package com.sunnychung.gradle.plugin.kotlite.apidoc

import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.CustomFunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.ExtensionProperty
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.GlobalProperty
import com.sunnychung.lib.multiplatform.kotlite.model.LibraryModule
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.stdlib.AllStdLibModules
import java.io.File

class GenerateApiDocTask {

    private val BLOCK_SEPARATOR = "========"

    private fun ClassDefinition.toKotlinHeaderCode() = buildString {
        modifiers.forEach {
            append("${it.name} ")
        }
        append(if (isInterface) "interface " else "class ")
        append(fullQualifiedName)
        if (typeParameters.isNotEmpty()) {
            append("<")
            append(typeParameters.joinToString {
                CodeGenerator(it).generateCode()
            })
            append(">")
        }
        if (primaryConstructor != null && primaryConstructor!!.parameters.isNotEmpty()) {
            append(" ")
            append(CodeGenerator(primaryConstructor!!, false).generateCode())
        }
        if (superClassInvocation != null || superInterfaceTypes.isNotEmpty()) {
            append(" : ")
            append((listOfNotNull(superClassInvocation) + superInterfaceTypes).joinToString {
                CodeGenerator(it).generateCode()
            })
        }
    }

    private fun PropertyDeclarationNode.toKotlinHeaderCode() = buildString {
        append(if (isMutable) "var " else "val ")
        if (typeParameters.isNotEmpty()) {
            append("<")
            append(typeParameters.joinToString {
                CodeGenerator(it).generateCode()
            })
            append("> ")
        }
        append(name)
        append(": ")
        append(CodeGenerator(type).generateCode())
    }

    private fun ClassDefinition.toApiDoc() = buildString {
        appendLine("[example]\n$BLOCK_SEPARATOR\n*${fullQualifiedName}*")
        appendLine("[source, kotlin]\n----")
        appendLine(toKotlinHeaderCode())
        appendLine("----")
        val memberProperties = declarations.filterIsInstance<PropertyDeclarationNode>()
        val memberFunctions = declarations.filterIsInstance<FunctionDeclarationNode>()
        if (memberProperties.isNotEmpty()) {
            append("\nMember Properties\n\n")
            memberProperties.forEach {
                append(formatKotlinHeaderCode(it.toKotlinHeaderCode()))
            }
        }
        if (memberFunctions.isNotEmpty()) {
            append("\nMember Functions\n\n")
            memberFunctions.forEach {
                append(formatKotlinHeaderCode(it.toKotlinHeaderCode()))
            }
        }
        appendLine(BLOCK_SEPARATOR)
    }

    private fun GlobalProperty.toKotlinHeaderCode() = buildString {
        append(if (isMutable) "var " else "val ")
        append("$declaredName: $type")
    }

    private fun ExtensionProperty.toKotlinHeaderCode() = buildString {
        append(if (setter != null) "var " else "val ")
        if (typeParameters.isNotEmpty()) {
            append("<")
            append(typeParameters.joinToString {
                it.name + if (it.typeUpperBound != null) {
                    " : ${it.typeUpperBound}"
                } else ""
            })
            append("> ")
        }
        append("$receiver.$declaredName: $type")
        if (getter != null) append(" get()")
        if (setter != null) append(" set()")
    }

    private fun FunctionDeclarationNode.toKotlinHeaderCode() =
        CodeGenerator(copy(body = null)).generateCode().removeSuffix(" {\n}")

    fun formatKotlinHeaderCode(code: String) =
//        "* `$code`"
        "[source, kotlin]\n----\n$code\n----\n"

    private fun generateApiDoc(
        classes: List<ClassDefinition>,
        globalProperties: List<GlobalProperty>,
        extensionProperties: List<ExtensionProperty>,
        functions: List<FunctionDeclarationNode>,
    ) = buildString {
        classes.filter { !it.fullQualifiedName.endsWith("?") && !it.fullQualifiedName.endsWith(".Companion") }.takeIf { it.isNotEmpty() }?.let { types ->
            append("=== Types\n\n")
            types.forEach {
                appendLine(it.toApiDoc())
            }
            appendLine()
        }
        globalProperties.takeIf { it.isNotEmpty() }?.let { properties ->
            append("=== Global Properties\n\n")
            properties.forEach {
                append(formatKotlinHeaderCode(it.toKotlinHeaderCode()))
            }
            appendLine()
        }
        extensionProperties.takeIf { it.isNotEmpty() }?.let { properties ->
            append("=== Extension Properties\n\n")
            properties.forEach {
                append(formatKotlinHeaderCode(it.toKotlinHeaderCode()))
            }
            appendLine()
        }
        functions.filter { it.receiver == null }.takeIf { it.isNotEmpty() }?.let { properties ->
            append("=== Global Functions\n\n")
            properties.forEach {
                append(formatKotlinHeaderCode(it.toKotlinHeaderCode()))
            }
            appendLine()
        }
        functions.filter { it.receiver != null }.takeIf { it.isNotEmpty() }?.let { properties ->
            append("=== Extension Functions\n\n")
            properties.forEach {
                append(formatKotlinHeaderCode(it.toKotlinHeaderCode()))
            }
            appendLine()
        }
    }

    private fun generateBuiltinApiDoc() = buildString {
        appendLine("== Built-in")
        appendLine()

        val env = ExecutionEnvironment()
        val symbolTable = SymbolTable(0, "", ScopeType.Script, null)

        appendLine(generateApiDoc(
            classes = env.getBuiltinClasses(symbolTable),
            globalProperties = env.getGlobalProperties(symbolTable),
            extensionProperties = env.getExtensionProperties(symbolTable),
            functions = env.getBuiltinFunctions(symbolTable),
        ))
    }

    private fun LibraryModule.toApiDoc() = buildString {
        appendLine("== $name")
        appendLine()
        appendLine(generateApiDoc(
            classes = classes,
            globalProperties = globalProperties,
            extensionProperties = properties,
            functions = functions.map { CustomFunctionDeclarationNode(it) },
        ))
    }

    fun generate() = buildString {
        appendLine("= Built-in and Standard Library APIs")
        appendLine()
        appendLine(generateBuiltinApiDoc())
        AllStdLibModules().modules.forEach {
            appendLine(it.toApiDoc())
        }
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        throw IllegalArgumentException("Requiring 1 parameter representing output file path")
    }
    val outputFilePath = args[0]
    val content = GenerateApiDocTask().generate()
    File(outputFilePath).parentFile.mkdirs()
    File(outputFilePath).writeText(content)
}
