package com.boswelja.contactringtonegenerator.contactpicker

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
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

object ContactsHelper {

    @VisibleForTesting(otherwise = PRIVATE)
    val CONTACTS_PROJECTION = arrayOf(
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
    fun getContacts(
        contentResolver: ContentResolver,
        filter: String? = null
    ): Flow<List<Contact>> = flow {
        val selection = filter?.let {
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
        }
        val selectionArgs = filter?.let { arrayOf("%$filter%") }
        Timber.d("Getting all contacts where %s matches %s", selection, filter)
        val contacts = mutableListOf<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACTS_PROJECTION,
            selection,
            selectionArgs,
            ContactsContract.Contacts.SORT_KEY_PRIMARY
        )
        cursor?.let {
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
            cursor.close()
        }
    }

    suspend fun getContactNickname(contentResolver: ContentResolver, lookupKey: String): String? {
        return withContext(Dispatchers.IO) {
            val cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                CONTACT_NICKNAME_PROJECTION,
                "${ContactsContract.Data.LOOKUP_KEY} = ? AND ${ContactsContract.CommonDataKinds.Nickname.MIMETYPE} = ?",
                arrayOf(lookupKey, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE), null
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

            return@withContext nickname
        }
    }

    suspend fun getContactStructuredName(
        contentResolver: ContentResolver,
        lookupKey: String
    ): StructuredName? {
        return withContext(Dispatchers.IO) {
            val cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                CONTACT_NAME_PROJECTION,
                "${ContactsContract.Data.LOOKUP_KEY} = ? AND ${ContactsContract.CommonDataKinds.StructuredName.MIMETYPE} = ?",
                arrayOf(
                    lookupKey,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                ),
                null
            )
            if (cursor == null || !cursor.moveToFirst()) {
                return@withContext null
            }
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
            val firstName = cursor.getStringOrNull(firstNameColumn)
            val middleName = cursor.getStringOrNull(middleNameColumn)
            val lastName = cursor.getStringOrNull(lastNameColumn)
            val prefix = cursor.getStringOrNull(prefixColumn)
            val suffix = cursor.getStringOrNull(suffixColumn)
            cursor.close()
            return@withContext StructuredName(prefix, firstName, middleName, lastName, suffix)
        }
    }

    suspend fun setContactRingtone(
        context: Context,
        contactUri: Uri,
        ringtoneUri: Uri
    ) {
        withContext(Dispatchers.IO) {
            val values = ContentValues()
            values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtoneUri.toString())
            context.contentResolver.update(contactUri, values, null, null)
        }
    }

    suspend fun getContactUri(
        context: Context,
        contactLookupKey: String
    ): Uri? {
        val lookupUri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_LOOKUP_URI, contactLookupKey
        )

        return ContactsContract.Contacts.lookupContact(context.contentResolver, lookupUri)
    }

    suspend fun removeContactRingtone(context: Context, contact: Contact) {
        withContext(Dispatchers.IO) {
            val contactUri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey)
            val values = ContentValues()
            values.putNull(ContactsContract.Contacts.CUSTOM_RINGTONE)
            context.contentResolver.update(contactUri, values, null, null)
        }
    }

    fun openContactPhotoStream(context: Context, contact: Contact): InputStream? {
        return ContactsContract.Contacts.openContactPhotoInputStream(
            context.contentResolver,
            contact.uri,
            false
        )
    }
}
