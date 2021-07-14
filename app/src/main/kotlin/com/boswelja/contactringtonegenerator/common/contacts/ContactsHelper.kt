package com.boswelja.contactringtonegenerator.common.contacts

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

private const val ContactDataTypeLookupSelection =
    "${ContactsContract.Data.LOOKUP_KEY} = ? AND ${ContactsContract.CommonDataKinds.Nickname.MIMETYPE} = ?" // ktlint-disable max-line-length

private val CONTACTS_PROJECTION = arrayOf(
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.LOOKUP_KEY,
    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
)

private val CONTACT_NAME_PROJECTION = arrayOf(
    ContactsContract.CommonDataKinds.StructuredName._ID,
    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
    ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
    ContactsContract.CommonDataKinds.StructuredName.PREFIX,
    ContactsContract.CommonDataKinds.StructuredName.SUFFIX
)

private val CONTACT_NICKNAME_PROJECTION = arrayOf(
    ContactsContract.CommonDataKinds.Nickname._ID,
    ContactsContract.CommonDataKinds.Nickname.MIMETYPE,
    ContactsContract.CommonDataKinds.Nickname.NAME
)

/**
 * Flow all contacts whose [ContactsContract.Contacts.DISPLAY_NAME_PRIMARY] contains [filter].
 * @param filter The filter string to match contacts to, or null if there is no filter.
 * @return a [Flow] of contacts matching the input filter.
 */
@ExperimentalCoroutinesApi
fun ContentResolver.getContacts(
    filter: String? = null
): Flow<List<Contact>> = flow {
    // Build selection parameters
    val selection = filter?.let {
        "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
    }
    val selectionArgs = filter?.let { arrayOf("%$filter%") }

    // Collect all contacts
    val contacts = mutableListOf<Contact>()
    query(
        ContactsContract.Contacts.CONTENT_URI,
        CONTACTS_PROJECTION,
        selection,
        selectionArgs,
        ContactsContract.Contacts.SORT_KEY_PRIMARY
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
        val lookupKeyColumn = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
        val displayNameColumn =
            cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        while (cursor.moveToNext() && currentCoroutineContext().isActive) {
            val id = cursor.getLong(idColumn)
            // Only continue if this contact is unique
            if (!contacts.any { it.id == id }) {
                val displayName = cursor.getStringOrNull(displayNameColumn)
                displayName?.let {
                    val lookupKey = cursor.getString(lookupKeyColumn)
                    val contact = Contact(
                        id,
                        lookupKey,
                        displayName,
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
                    )
                    contacts.add(contact)
                }
            }
        }
    }

    // Send contacts on finished
    emit(contacts)
}

/**
 * Get a contact's nickname, or empty of the contact has no nickname set.
 * @param lookupKey See [Contact.lookupKey].
 * @return The contact's nickname, or an empty string if the contact has no nickname.
 */
suspend fun ContentResolver.getContactNickname(lookupKey: String): String {
    return withContext(Dispatchers.IO) {
        val cursor = query(
            ContactsContract.Data.CONTENT_URI,
            CONTACT_NICKNAME_PROJECTION,
            ContactDataTypeLookupSelection,
            arrayOf(lookupKey, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE),
            null
        )

        var nickname: String? = null
        if (cursor != null) {
            val nicknameColumn =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)
            if (cursor.moveToFirst()) {
                nickname = cursor.getStringOrNull(nicknameColumn)
            }
            cursor.close()
        }

        return@withContext nickname ?: ""
    }
}

/**
 * Get the [StructuredName] for a contact.
 * @param lookupKey See [Contact.lookupKey].
 * @return A [StructuredName] for the contact, or null if the contact couldn't be found.
 */
suspend fun ContentResolver.getContactStructuredName(
    lookupKey: String
): StructuredName? {
    return withContext(Dispatchers.IO) {
        query(
            ContactsContract.Data.CONTENT_URI,
            CONTACT_NAME_PROJECTION,
            ContactDataTypeLookupSelection,
            arrayOf(
                lookupKey,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            ),
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val firstNameColumn =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
            val middleNameColumn =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
            val lastNameColumn =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
            val prefixColumn =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX)
            val suffixColumn =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)
            val firstName = cursor.getStringOrNull(firstNameColumn) ?: ""
            val middleName = cursor.getStringOrNull(middleNameColumn) ?: ""
            val lastName = cursor.getStringOrNull(lastNameColumn) ?: ""
            val prefix = cursor.getStringOrNull(prefixColumn) ?: ""
            val suffix = cursor.getStringOrNull(suffixColumn) ?: ""
            return@use StructuredName(prefix, firstName, middleName, lastName, suffix)
        }
    }
}

/**
 * Sets a contact's custom ringtone.
 * @param contactUri See [Contact.uri].
 * @param ringtoneUri The [Uri] of the ringtone stored in MediaStore.
 */
suspend fun ContentResolver.setContactRingtone(
    contactUri: Uri,
    ringtoneUri: Uri
) {
    withContext(Dispatchers.IO) {
        val values = ContentValues()
        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtoneUri.toString())
        update(contactUri, values, null, null)
    }
}

/**
 * Get a lookup Uri for a contact.
 * @param contactLookupKey See [Contact.lookupKey].
 * @return The lookup Uri for the contact, or null if the contact wasn't found.
 */
suspend fun ContentResolver.getContactUri(
    contactLookupKey: String
): Uri? {
    return withContext(Dispatchers.IO) {
        val lookupUri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_LOOKUP_URI, contactLookupKey
        )

        ContactsContract.Contacts.lookupContact(this@getContactUri, lookupUri)
    }
}

/**
 * Clear a contact's custom ringtone.
 * @param contact The [Contact] to clear the custom ringtone for.
 */
suspend fun ContentResolver.removeRingtoneFor(contact: Contact) {
    withContext(Dispatchers.IO) {
        val contactUri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey)
        val values = ContentValues()
        values.putNull(ContactsContract.Contacts.CUSTOM_RINGTONE)
        update(contactUri, values, null, null)
    }
}

/**
 * Open an [InputStream] for the contact's photo, if there is one.
 * @param contact The [Contact] whose photo we should open.
 * @return The [InputStream] for the contact photo, or null if there is no photo.
 */
fun ContentResolver.openContactPhotoStream(contact: Contact): InputStream? {
    return ContactsContract.Contacts.openContactPhotoInputStream(
        this,
        contact.uri,
        false
    )
}
