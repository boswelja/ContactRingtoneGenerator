package com.boswelja.contactringtonegenerator.contacts

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.InputStream

object ContactsHelper {

    @VisibleForTesting(otherwise = PRIVATE)
    val CONTACTS_PROJECTION = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY
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
        pageSize: Int
    ): Flow<List<Contact>> = callbackFlow {
        val contacts = mutableListOf<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACTS_PROJECTION,
            null,
            null,
            ContactsContract.Contacts.SORT_KEY_PRIMARY
        )
        var currentGrowth = 0
        cursor?.let {
            val idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val lookupKeyColumn = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val lookupKey = cursor.getString(lookupKeyColumn)
                getContactStructuredName(contentResolver, lookupKey)?.let { structuredName ->
                    val contact =
                        Contact(
                            id,
                            lookupKey,
                            structuredName[1],
                            structuredName[3],
                            structuredName[2],
                            structuredName[0],
                            structuredName[4],
                            getContactNickname(contentResolver, lookupKey)
                        )
                    if (!contacts.any { it.id == contact.id }) {
                        contacts.add(contact)
                        currentGrowth++
                        if (currentGrowth >= pageSize) {
                            send(contacts)
                            currentGrowth = 0
                        }
                    }
                }
            }
            // Send contacts on finished anyways
            send(contacts)
            cursor.close()
        }

        awaitClose {
            if (cursor?.isClosed == false) cursor.close()
        }
    }

    private suspend fun getContactNickname(contentResolver: ContentResolver, lookupKey: String): String? {
        return withContext(Dispatchers.IO) {
            val cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                CONTACT_NICKNAME_PROJECTION,
                "${ContactsContract.Data.LOOKUP_KEY} = ? AND ${ContactsContract.CommonDataKinds.Nickname.MIMETYPE} = ?",
                arrayOf(lookupKey, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE), null
            )

            var nickname: String? = null
            if (cursor != null) {
                val nicknameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)
                if (cursor.moveToFirst()) {
                    nickname = cursor.getStringOrNull(nicknameColumn)
                }
                cursor.close()
            }

            return@withContext nickname
        }
    }

    private suspend fun getContactStructuredName(contentResolver: ContentResolver, lookupKey: String): Array<String?>? {
        return withContext(Dispatchers.IO) {
            val cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                CONTACT_NAME_PROJECTION,
                "${ContactsContract.Data.LOOKUP_KEY} = ? AND ${ContactsContract.CommonDataKinds.StructuredName.MIMETYPE} = ?",
                arrayOf(lookupKey, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE), null
            )
            if (cursor == null || !cursor.moveToFirst()) {
                return@withContext null
            }
            val firstNameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
            val middleNameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
            val lastNameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
            val prefixColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX)
            val suffixColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)
            val firstName = cursor.getString(firstNameColumn)
            val middleName = cursor.getStringOrNull(middleNameColumn)
            val lastName = cursor.getStringOrNull(lastNameColumn)
            val prefix = cursor.getStringOrNull(prefixColumn)
            val suffix = cursor.getStringOrNull(suffixColumn)
            cursor.close()
            return@withContext arrayOf(prefix, firstName, middleName, lastName, suffix)
        }
    }

    suspend fun setContactRingtone(context: Context, contact: Contact, ringtoneUri: Uri) {
        withContext(Dispatchers.IO) {
            val contactUri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey)
            val values = ContentValues()
            values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtoneUri.toString())
            context.contentResolver.update(contactUri, values, null, null)
        }
    }

    suspend fun removeContactRingtone(context: Context, contact: Contact) {
        withContext(Dispatchers.IO) {
            val contactUri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey)
            val values = ContentValues()
            values.putNull(ContactsContract.Contacts.CUSTOM_RINGTONE)
            context.contentResolver.update(contactUri, values, null, null)
        }
    }

    fun openContactPhotoStream(context: Context, contactId: Long): InputStream? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        return ContactsContract.Contacts.openContactPhotoInputStream(
            context.contentResolver,
            contactUri,
            false
        )
    }
}
