package com.boswelja.contactringtonegenerator.common.mediastore

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

private val DELETE_RINGTONE_PROJECTION = arrayOf(
    MediaStore.Audio.Media.DISPLAY_NAME,
    MediaStore.Audio.Media.IS_RINGTONE
)

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
    return withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, file.name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.IS_PENDING, 1)
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES)
            }
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/ogg")
            put(MediaStore.Audio.Media.IS_RINGTONE, true)
        }

        // Delete the ringtone if it already exists
        deleteRingtones(arrayOf(file.name))

        return@withContext try {
            val uri = insert(RINGTONE_COLLECTION, values)
            uri?.let {
                FileInputStream(file).use { inStream ->
                    openOutputStream(it)?.use { outStream ->
                        var byte = inStream.read()
                        while (byte != -1) {
                            outStream.write(byte)
                            byte = inStream.read()
                        }
                        outStream.close()
                    }
                    inStream.close()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    update(it, values, null, null)
                }
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
    Timber.d("deleteAllRingtones() called")
    withContext(Dispatchers.IO) {
        val displayNameArray = ArrayList<String>()
        val query = query(
            RINGTONE_COLLECTION,
            DELETE_RINGTONE_PROJECTION,
            null,
            null,
            null
        )
        if (query != null && query.moveToFirst()) {
            val displayNameCol = query.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            do {
                val displayName = query.getString(displayNameCol)
                Timber.d("Inspecting $displayName")
                // TODO This isn't right
                if (displayName.endsWith("-generated-ringtone.ogg")) {
                    displayNameArray.add(displayName)
                }
            } while (query.moveToNext())
            query.close()
        } else {
            Timber.w("Query null or empty")
        }
        deleteRingtones(displayNameArray.toTypedArray())
    }
}

/**
 * Delete an array of ringtones.
 * @param fileNames The file names of all the ringtones to delete.
 */
suspend fun ContentResolver.deleteRingtones(fileNames: Array<String>) {
    withContext(Dispatchers.IO) {
        Timber.d("Deleting ${fileNames.count()} ringtones")
        try {
            delete(
                RINGTONE_COLLECTION,
                "${MediaStore.Audio.Media.DISPLAY_NAME} = ?",
                fileNames
            )
            fileNames.map { File(it) }.forEach {
                try {
                    it.delete()
                } catch (ignored: Exception) {}
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
    }
}
