package com.boswelja.contactringtonegenerator.mediastore

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream

object MediaStoreHelper {

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
     * @param context [Context].
     * @param file The [File] to scan.
     * @return The [Uri] for the scanned file, or null if scanning failed.
     */
    suspend fun scanNewFile(context: Context, file: File): Uri? {
        return withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, file.name)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(MediaStore.Audio.Media.IS_PENDING, 1)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/ogg")
                put(MediaStore.Audio.Media.IS_RINGTONE, true)
            }

            // Delete the ringtone if it already exists
            deleteRingtones(context, arrayOf(file.name))

            val uri = contentResolver.insert(RINGTONE_COLLECTION, values)
            uri?.let {
                FileInputStream(file).use { inStream ->
                    contentResolver.openOutputStream(it)?.use { outStream ->
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
                    contentResolver.update(it, values, null, null)
                }
            }
            return@withContext uri
        }
    }

    /**
     * Deletes all ringtones created by this app.
     * @param context [Context].
     */
    suspend fun deleteAllRingtones(context: Context) {
        Timber.d("deleteAllRingtones() called")
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val displayNameArray = ArrayList<String>()
            val query = contentResolver.query(RINGTONE_COLLECTION, DELETE_RINGTONE_PROJECTION, null, null, null)
            if (query != null && query.moveToFirst()) {
                val displayNameCol = query.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                do {
                    val displayName = query.getString(displayNameCol)
                    Timber.d("Inspecting $displayName")
                    if (displayName.endsWith("-generated-ringtone.ogg")) {
                        displayNameArray.add(displayName)
                    }
                } while (query.moveToNext())
                query.close()
            } else {
                Timber.w("Query null or empty")
            }
            deleteRingtones(context, displayNameArray.toTypedArray())
        }
    }

    /**
     * Delete a single ringtone.
     * @param context [Context].
     * @param fileNames The file names of all the ringtones to delete.
     */
    suspend fun deleteRingtones(context: Context, fileNames: Array<String>) {
        Timber.d("Deleting ${fileNames.count()} ringtones")
        try {
            context.contentResolver.delete(RINGTONE_COLLECTION, "${MediaStore.Audio.Media.DISPLAY_NAME} = ?", fileNames)
        } catch (e: Exception) {
            Timber.w(e)
        }
    }
}
