package com.boswelja.contactringtonegenerator.common.contacts

import android.net.Uri

/**
 * Basic information about a contact from the device.
 * @param id See [android.provider.ContactsContract.Contacts._ID]
 * @param lookupKey See [android.provider.ContactsContract.Contacts.LOOKUP_KEY]
 * @param displayName See [android.provider.ContactsContract.Contacts.DISPLAY_NAME_PRIMARY]
 * @param uri The contact's content Uri.
 */
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
