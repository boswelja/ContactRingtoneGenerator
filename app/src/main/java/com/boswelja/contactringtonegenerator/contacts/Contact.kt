package com.boswelja.contactringtonegenerator.contacts

import java.util.StringJoiner

data class Contact(
    val id: Long,
    val lookupKey: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val prefix: String? = null,
    val suffix: String? = null,
    val nickname: String? = null
) {
    val displayName: String =
        StringJoiner(" ").apply {
            if (prefix != null) add(prefix)
            if (firstName != null) add(firstName)
            if (middleName != null) add(middleName)
            if (lastName != null) add(lastName)
            if (suffix != null) add(suffix)
        }.toString()

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
