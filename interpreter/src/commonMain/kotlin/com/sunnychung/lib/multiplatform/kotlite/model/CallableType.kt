package com.sunnychung.lib.multiplatform.kotlite.model

enum class CallableType(val order: Int /* the least order the greatest priority */) {
    Property(5), Function(2), ExtensionFunction(4), ClassMemberFunction(1), Constructor(3)
}
