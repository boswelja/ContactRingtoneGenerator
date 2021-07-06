package com.boswelja.contactringtonegenerator.contactpicker

import android.net.Uri

data class Contact(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val uri: Uri
) {

    override fun equals(other: Any?): Boolean {
        if (other is Contact) {
            return other.id == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
