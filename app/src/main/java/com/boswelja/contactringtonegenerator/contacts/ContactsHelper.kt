package com.boswelja.contactringtonegenerator.contacts

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContactsHelper {

    private val CONTACTS_PROJECTION = arrayOf(
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

    suspend fun getContacts(context: Context): List<Contact> {
        val contacts = ArrayList<Contact>()
        withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                CONTACTS_PROJECTION, null, null, null
            )
            if (cursor != null) {
                val idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val lookupKeyColumn = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val lookupKey = cursor.getString(lookupKeyColumn)
                    val structuredName = getContactStructuredName(context, lookupKey)!!
                    val contact =
                        Contact(
                            id,
                            lookupKey,
                            structuredName[1]!!,
                            structuredName[3],
                            structuredName[2],
                            structuredName[0],
                            structuredName[4],
                            getContactNickname(context, lookupKey),
                            getContactPhotoUri(context, id)
                        )
                    if (!contacts.any { it.id == contact.id }) {
                        contacts.add(contact)
                    }
                }
                cursor.close()
            }
        }
        return withContext(Dispatchers.Default) {
            return@withContext contacts.sortedBy { it.nickname ?: it.firstName }
        }
    }

    private suspend fun getContactNickname(context: Context, lookupKey: String): String? {
        return withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
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

    private suspend fun getContactPhotoUri(context: Context, contactId: Long): Uri? {
        return withContext(Dispatchers.IO) {
            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
            val cursor = context.contentResolver.query(photoUri, arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null)
            if (cursor == null || !cursor.moveToFirst()) {
                return@withContext null
            }
            cursor.close()
            return@withContext photoUri
        }
    }

    private suspend fun getContactStructuredName(context: Context, lookupKey: String): Array<String?>? {
        return withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
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
}
