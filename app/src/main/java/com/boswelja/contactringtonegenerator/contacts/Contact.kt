package com.boswelja.contactringtonegenerator.contacts

import android.net.Uri

data class Contact(
        val id: Long,
        val lookupKey: String,
        val name: String,
        val nickname: String? = null,
        val photoUri: Uri? = null
)