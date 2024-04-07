package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.annotation.ModifyByAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.error.IdentifierClassifier
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer

class GlobalProperty(
    val position: SourcePosition,
    val declaredName: String,
    val type: String,
    val isMutable: Boolean,
    val getter: ((interpreter: Interpreter) -> RuntimeValue)? = null,
    val setter: ((interpreter: Interpreter, value: RuntimeValue) -> Unit)? = null,
) {
    internal var typeNode: TypeNode? = null

    init {
        if (getter == null && setter == null) {
            throw IllegalArgumentException("Missing getter or setter")
        }
        if (!isMutable && getter == null) {
            throw IllegalArgumentException("Missing getter")
        }
        typeNode = Parser(Lexer(position.filename, type)).type()
    }

    internal @ModifyByAnalyzer var transformedName: String? = null
    internal var dataType: DataType? = null
    internal var interpreter: Interpreter? = null
    internal lateinit var accessor: RuntimeValueAccessor
        private set

    fun attachToSemanticAnalyzer(semanticAnalyzer: SemanticAnalyzer) {
        with (semanticAnalyzer) {
            generateTransformedName()
            dataType = currentScope.assertToDataType(typeNode!!)
            currentScope.registerTransformedSymbol(
                position = position,
                identifierClassifier = IdentifierClassifier.Property,
                transformedName = transformedName!!,
                originalName = declaredName,
            )
        }
        initAccessor()
    }

    fun attachToInterpreter(interpreter: Interpreter) {
        this.interpreter = interpreter
        dataType = interpreter.symbolTable().assertToDataType(typeNode!!)
        interpreter.symbolTable().registerTransformedSymbol(
            position = position,
            identifierClassifier = IdentifierClassifier.Property,
            transformedName = transformedName!!,
            originalName = declaredName,
        )
        initAccessor()
    }

    private fun initAccessor() {
        accessor = object : RuntimeValueAccessor {
            override val type: DataType = dataType ?: throw RuntimeException("Global property `$declaredName` is not initiailized")

            override fun assign(z: Interpreter?, value: RuntimeValue) {
                if (interpreter == null) return // Semantic Analyzer would assign a dummy value
                setter?.invoke(interpreter!!, value)
                    ?: throw NotImplementedError("Setter is not implemented for the global property `$declaredName`")
            }

            override fun read(z: Interpreter?): RuntimeValue {
                return getter?.invoke(interpreter!!)
                    ?: throw NotImplementedError("Getter is not implemented for the global property `$declaredName`")
            }
        }
    }
}
