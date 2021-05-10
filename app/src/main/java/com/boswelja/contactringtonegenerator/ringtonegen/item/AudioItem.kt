package com.boswelja.contactringtonegenerator.ringtonegen.item

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import com.boswelja.contactringtonegenerator.R

sealed class AudioItem(id: ID) : StructureItem(id) {

    abstract val fallbackStringRes: Int

    var audioUri: Uri? = null
        private set
    var displayText: String? = null
        protected set

    override val icon = Icons.Filled.Audiotrack
    override val isDataValid: Boolean
        get() = audioUri != null

    private fun getDisplayText(context: Context, uri: Uri?): String {
        if (uri != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val name = cursor.getString(nameIndex)
                cursor.close()
                return name
            }
        }
        return context.getString(fallbackStringRes)
    }

    fun setAudioUri(context: Context, uri: Uri?) {
        audioUri = uri
        displayText = getDisplayText(context, uri)
    }

    class SystemRingtone : AudioItem(ID.SYSTEM_RINGTONE) {

        override val fallbackStringRes: Int = R.string.item_ringtone_no_file
        override val labelRes = R.string.label_system_ringtone
    }

    class File : AudioItem(ID.CUSTOM_AUDIO) {

        override val fallbackStringRes = R.string.item_audio_no_file
        override val labelRes = R.string.label_custom_audio
    }
}
