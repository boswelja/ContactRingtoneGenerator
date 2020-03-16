package com.boswelja.contactringtonegenerator.mediastore

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.boswelja.contactringtonegenerator.contacts.ContactManager
import com.boswelja.contactringtonegenerator.contacts.ContactRingtone
import java.io.FileInputStream

object MediaStoreManager {

    fun scanNewFiles(context: Context, contactRingtones: List<ContactRingtone>) {
        Log.d("MediaStoreManager", "Scanning ${contactRingtones.size} new files..")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentResolver = context.contentResolver
            val ringtoneCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            contactRingtones.forEach { contactRingtone ->
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, contactRingtone.contact.contactName)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "Ringtones/")
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/ogg")
                    put(MediaStore.Audio.Media.IS_RINGTONE, true)
                }
                val uri = contentResolver.insert(ringtoneCollection, values)
                uri?.let {
                    FileInputStream(contactRingtone.ringtoneFile).use { inStream ->
                        contentResolver.openOutputStream(it)?.use { outStream ->
                            var byte = inStream.read()
                            while (byte != -1) {
                                outStream.write(byte)
                                byte = inStream.read()
                            }
                            Log.d("MediaStoreManager", "Saved $it")
                            outStream.close()
                        }
                        inStream.close()
                    }
                    values.clear()
                    values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    contentResolver.update(it, values, null, null)
                    ContactManager.setContactRingtone(context, contactRingtone.contact, it)
                } ?: Log.d("MediaStoreManager", "uri null")
            }
        } else {
            val outFiles = contactRingtones.map { it.ringtoneFile.absolutePath }.toTypedArray()
            if (!outFiles.isNullOrEmpty()) {
                MediaScannerConnection(context, object : MediaScannerConnection.MediaScannerConnectionClient {
                    override fun onMediaScannerConnected() {
                        MediaScannerConnection.scanFile(
                                context,
                                outFiles,
                                null,
                                this
                        )
                    }

                    override fun onScanCompleted(path: String?, uri: Uri?) {
                        if (uri != null) {
                            val contact = contactRingtones.firstOrNull { it.ringtoneFile.absolutePath == path }?.contact
                            if (contact != null) {
                                ContactManager.setContactRingtone(context, contact, uri)
                            }
                        }
                    }
                }).also {
                    it.connect()
                }
            }
        }
    }
}