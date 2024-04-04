package com.sunnychung.lib.multiplatform.kotlite

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

internal val log = Logger.apply {
    setTag("kotlite")
    setMinSeverity(Severity.Info)
}

fun setKotliteLogMinLevel(severity: Severity) {
    log.setMinSeverity(severity)
}
