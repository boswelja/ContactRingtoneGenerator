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
import timber.log.Timber

private const val ContactDataTypeLookupSelection =
    "${ContactsContract.Data.LOOKUP_KEY} = ? AND ${ContactsContract.CommonDataKinds.Nickname.MIMETYPE} = ?"

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

@ExperimentalCoroutinesApi
fun ContentResolver.getContacts(
    filter: String? = null
): Flow<List<Contact>> = flow {
    val selection = filter?.let {
        "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
    }
    val selectionArgs = filter?.let { arrayOf("%$filter%") }
    Timber.d("Getting all contacts where %s matches %s", selection, filter)
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
        // Send contacts on finished
        emit(contacts)
    }
}

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

suspend fun ContentResolver.getContactUri(
    contactLookupKey: String
): Uri? {
    val lookupUri = Uri.withAppendedPath(
        ContactsContract.Contacts.CONTENT_LOOKUP_URI, contactLookupKey
    )

    return ContactsContract.Contacts.lookupContact(this, lookupUri)
}

suspend fun ContentResolver.removeRingtoneFor(contact: Contact) {
    withContext(Dispatchers.IO) {
        val contactUri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey)
        val values = ContentValues()
        values.putNull(ContactsContract.Contacts.CUSTOM_RINGTONE)
        update(contactUri, values, null, null)
    }
}

fun ContentResolver.openContactPhotoStream(contact: Contact): InputStream? {
    return ContactsContract.Contacts.openContactPhotoInputStream(
        this,
        contact.uri,
        false
    )
}
