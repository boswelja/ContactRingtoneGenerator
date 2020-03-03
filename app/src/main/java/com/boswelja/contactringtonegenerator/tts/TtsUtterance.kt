package com.boswelja.contactringtonegenerator.tts

import com.boswelja.contactringtonegenerator.contacts.Contact
import java.io.File

data class TtsUtterance(
    val utteranceId: String,
    val personalisedMessage: String,
    val contact: Contact,
    val file: File
)