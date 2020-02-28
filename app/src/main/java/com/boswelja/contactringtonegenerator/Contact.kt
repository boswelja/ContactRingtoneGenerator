package com.boswelja.contactringtonegenerator

data class Contact(
    val id: Long,
    val lookupKey: String,
    val contactName: String,
    val contactNickname: String? = null
)