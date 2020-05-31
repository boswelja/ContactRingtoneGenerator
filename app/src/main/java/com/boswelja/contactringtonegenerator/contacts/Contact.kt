package com.boswelja.contactringtonegenerator.contacts

import android.net.Uri

data class Contact(
    val id: Long,
    val lookupKey: String,
    val photoUri: Uri?,
    val contactName: String,
    val contactNickname: String? = null
)