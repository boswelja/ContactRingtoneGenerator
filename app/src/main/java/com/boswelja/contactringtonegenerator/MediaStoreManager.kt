package com.boswelja.contactringtonegenerator

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log

object MediaStoreManager {

    fun scanNewFiles(context: Context, contactRingtones: List<ContactRingtone>) {
        val outFiles = contactRingtones.map { it.ringtonePath }.toTypedArray()
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
                        val contact = contactRingtones.firstOrNull { it.ringtonePath == path }?.contact
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