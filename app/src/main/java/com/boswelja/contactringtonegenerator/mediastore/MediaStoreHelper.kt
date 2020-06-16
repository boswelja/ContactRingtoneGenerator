package com.boswelja.contactringtonegenerator.mediastore

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

object MediaStoreHelper {

    /**
     * Scan a ringtone file into the [MediaStore].
     * @param context [Context].
     * @param file The [File] to scan.
     * @return The [Uri] for the scanned file, or null if scanning failed.
     */
    suspend fun scanNewFile(context: Context, file: File): Uri? {
        return withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val ringtoneCollection =if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, file.name)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(MediaStore.Audio.Media.IS_PENDING, 1)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/ogg")
                put(MediaStore.Audio.Media.IS_RINGTONE, true)
            }
            val uri = contentResolver.insert(ringtoneCollection, values)
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
}