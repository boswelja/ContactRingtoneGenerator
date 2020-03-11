package com.boswelja.contactringtonegenerator.mediastore

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import com.boswelja.contactringtonegenerator.contacts.ContactManager
import com.boswelja.contactringtonegenerator.contacts.ContactRingtone

object MediaStoreManager {

    fun scanNewFiles(context: Context, contactRingtones: List<ContactRingtone>) {
        val outFiles = contactRingtones.map { it.ringtoneFile.absolutePath }.toTypedArray()
        Log.d("MediaStoreManager", "Scanning ${outFiles.size} new files..")
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