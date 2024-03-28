package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer

class SpecialFunction(
    val function: FunctionDeclarationNode
) {
    fun call(interpreter: Interpreter, subject: RuntimeValue, arguments: List<RuntimeValue>): RuntimeValue {
        return with(interpreter) {
            FunctionCallNode(
                function = function,
                arguments = arguments.mapIndexed { index, it ->
                    FunctionCallArgumentNode(
                        position = SourcePosition.NONE,
                        index = index,
                        value = ValueNode(SourcePosition.NONE, it)
                    )
                },
                declaredTypeArguments = emptyList(),
                position = SourcePosition.NONE,
            ).evalClassMemberAnyFunctionCall(subject, function)
        }
    }

    companion object {
        private val parseType = { code: String ->
            Parser(Lexer(BuiltinFilename.BUILTIN, code)).type(
                isTryParenthesizedType = true,
                isParseDottedIdentifiers = true,
                isIncludeLastIdentifierAsTypeName = true,
            )
        }
    }

    /**
     * @param acceptableValueParameterTypes ordered list of acceptable value parameters, which are wrapped by a list
     */
    enum class Name(val functionName: String, val acceptableValueParameterTypes: List<List<TypeNode>>) {
        Equals(functionName = "equals", acceptableValueParameterTypes = listOf(listOf(parseType("Any?")))),
        HashCode(functionName = "hashCode", acceptableValueParameterTypes = listOf(emptyList())),
        ToString(functionName = "toString", acceptableValueParameterTypes = listOf(emptyList())),
    }
}
