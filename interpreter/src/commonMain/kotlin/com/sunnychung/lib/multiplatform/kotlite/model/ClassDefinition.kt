package com.sunnychung.lib.multiplatform.kotlite.model

class ClassDefinition(
    currentScope: SymbolTable,

    val name: String,
    val fullQualifiedName: String = name, // TODO

    /**
     * If it is an object class, no new instance can be created
     */
    val isInstanceCreationAllowed: Boolean,

    /**
     * Only contains ClassInstanceInitializerNode and PropertyDeclarationNode
     */
    val orderedInitializersAndPropertyDeclarations: List<ASTNode>,

    // key = original name
    rawMemberProperties: List<PropertyDeclarationNode>,
    val memberFunctions: Map<String, FunctionDeclarationNode>,

    val primaryConstructor: ClassPrimaryConstructorNode?,
) {
    val memberProperties: Map<String, PropertyType> = rawMemberProperties.associate {
        it.name to (currentScope.typeNodeToPropertyType(
            it.type,
            it.isMutable
        ) ?: if (it.type.name == name) {
            PropertyType(ObjectType(this, it.type.isNullable), it.isMutable)
        } else throw RuntimeException("Unknown type ${it.type.name}"))
    }
    val memberPropertiesByTransformedName: Map<String, PropertyType> = rawMemberProperties
        .filter { it.transformedRefName != null }
        .associate {
            it.transformedRefName!! to memberProperties[it.name]!!
        }
    val memberPropertyNameToTransformedName: Map<String, String> = rawMemberProperties
        .filter { it.transformedRefName != null }
        .associate { it.name to it.transformedRefName!! }
}
