package com.boswelja.contactringtonegenerator.contacts

import java.io.File

data class ContactRingtone(
        val contact: Contact,
        val id: String,
        val ringtoneFile: File
)