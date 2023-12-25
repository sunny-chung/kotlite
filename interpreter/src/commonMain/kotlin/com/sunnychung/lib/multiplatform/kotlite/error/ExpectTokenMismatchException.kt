package com.sunnychung.lib.multiplatform.kotlite.error

import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition

class ExpectTokenMismatchException(expected: String, position: SourcePosition)
    : ParseException("Expected token $expected at line ${position.lineNum} col ${position.col}")
