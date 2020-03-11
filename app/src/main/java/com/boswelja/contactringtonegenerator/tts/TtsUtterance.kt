package com.boswelja.contactringtonegenerator.tts

import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactRingtone
import java.io.File

data class TtsUtterance(
    private val contactRingtone: ContactRingtone,
    val personalisedMessage: String
) {
    val utteranceId: String get() = contactRingtone.id
    val ringtoneFile: File get() = contactRingtone.ringtoneFile
    val contact: Contact get() = contactRingtone.contact
}