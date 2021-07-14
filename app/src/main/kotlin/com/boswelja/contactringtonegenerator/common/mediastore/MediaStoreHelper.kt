package com.boswelja.contactringtonegenerator.common.mediastore

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val RingtoneNameSuffix = "(Generated)"
private const val RingtoneNameSelection =
    "${MediaStore.Audio.Media.IS_RINGTONE} = 1 AND ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"

private val RINGTONE_COLLECTION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
} else {
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
}

/**
 * Scan a ringtone file into the [MediaStore].
 * @param file The [File] to scan.
 * @return The [Uri] for the scanned file, or null if scanning failed.
 */
suspend fun ContentResolver.scanRingtone(file: File): Uri? {
    val displayName = "${file.nameWithoutExtension} $RingtoneNameSuffix.${file.extension}"
    // Create ContentValues for this ringtone
    val values = ContentValues().apply {
        put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Audio.Media.IS_PENDING, 1)
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES)
        }
        put(MediaStore.Audio.Media.MIME_TYPE, "audio/ogg")
        put(MediaStore.Audio.Media.IS_RINGTONE, true)
    }

    // Move to IO dispatcher
    return withContext(Dispatchers.IO) {
        // Delete the ringtone if it already exists
        deleteRingtoneIfStored(displayName)

        try {
            // Try inserting ringtone into MediaStore
            val uri = insert(RINGTONE_COLLECTION, values) ?: return@withContext null
            // TOD OWhat can we do about this blocking call
            openFileDescriptor(uri, "w").use { descriptor ->
                // TODO Let the provider know about any errors
                if (descriptor == null) return@withContext null
                FileOutputStream(descriptor.fileDescriptor).use { outStream ->
                    file.inputStream().use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
            }

            // Clear pending flag if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                update(uri, values, null, null)
            }
            uri
        } catch (e: Exception) {
            Timber.w(e)
            null
        }
    }
}

/**
 * Deletes all ringtones created by this app.
 */
suspend fun ContentResolver.deleteGeneratedRingtones() {
    withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // If we're on API Q, we can just delete all from our package
            delete(
                RINGTONE_COLLECTION,
                "${MediaStore.Audio.Media.OWNER_PACKAGE_NAME} = ?",
                arrayOf("com.boswelja.contactringtonegenerator")
            )
        } else {
            delete(
                RINGTONE_COLLECTION,
                RingtoneNameSelection,
                arrayOf(RingtoneNameSuffix)
            )
        }
    }
}

/**
 * Delete a single ringtone, if it exists in the media store.
 * @param displayName The display name of the ringtone to try delete.
 */
suspend fun ContentResolver.deleteRingtoneIfStored(displayName: String) {
    withContext(Dispatchers.IO) {
        try {
            delete(
                RINGTONE_COLLECTION,
                RingtoneNameSelection,
                arrayOf(displayName)
            )
        } catch (e: Exception) {
            Timber.w(e)
        }
    }
}
