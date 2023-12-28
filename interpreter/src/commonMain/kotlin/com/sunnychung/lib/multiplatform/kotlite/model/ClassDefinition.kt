package com.sunnychung.lib.multiplatform.kotlite.model

class ClassDefinition(
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
    val memberProperties: Map<String, PropertyDeclarationNode>,
    val memberFunctions: Map<String, FunctionDeclarationNode>,

    val primaryConstructor: ClassPrimaryConstructorNode?,
) {
    val memberPropertiesByTransformedName: Map<String, PropertyDeclarationNode> = memberProperties.values.associateBy { it.transformedRefName!! }
}
