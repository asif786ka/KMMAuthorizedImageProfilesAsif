package com.asif.kmmauthorizedimageprofiles

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter

object KermitLogger {
    private val logger = Logger(
        config = StaticConfig(logWriterList = listOf(platformLogWriter())),
        tag = "KMMAuthorizedImageProfiles"
    )

    fun logDebug(message: String) {
        logger.d { message }
    }

    fun logError(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.e(throwable) { message }
        }
    }
}
