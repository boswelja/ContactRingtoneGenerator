package com.boswelja.contactringtonegenerator

import java.io.File

data class TtsUtterance(
    val utteranceId: String,
    val personalisedMessage: String,
    val contact: Contact,
    val file: File
)