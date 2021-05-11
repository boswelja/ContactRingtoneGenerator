package com.boswelja.contactringtonegenerator.ringtonebuilder

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object Utils {

    fun getDisplayText(context: Context, uri: Uri?): String? {
        if (uri != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val name = cursor.getString(nameIndex)
                cursor.close()
                return name
            }
        }
        return null
    }
}
