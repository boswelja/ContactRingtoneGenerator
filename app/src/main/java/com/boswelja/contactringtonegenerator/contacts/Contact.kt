package com.boswelja.contactringtonegenerator.contacts

import android.net.Uri

data class Contact(
    val id: Long,
    val lookupKey: String,
    val name: String,
    val nickname: String? = null,
    val photoUri: Uri? = null
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
