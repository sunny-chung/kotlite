package com.sunnychung.lib.multiplatform.kotlite.stdlib

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

internal val log = Logger.apply {
    setTag("kotlite-stdlib")
    setMinSeverity(Severity.Info)
}
