package com.boswelja.contactringtonegenerator.ringtonegen.item

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

sealed class AudioItem(id: ID) : StructureItem(id) {

    abstract val fallbackStringRes: Int

    var audioUri: Uri? = null
        private set
    var displayText: String? = null
        protected set

    override val isUserAdjustable: Boolean = true
    override fun getIconRes(): Int = iconRes

    protected fun getDisplayText(context: Context, uri: Uri?): String {
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

    companion object {
        const val iconRes: Int = R.drawable.structure_ic_audio
    }

    class SystemRingtone() : AudioItem(ID.SYSTEM_RINGTONE) {

        constructor(context: Context) : this() {
            displayText = getDisplayText(context, null)
        }

        override val fallbackStringRes: Int = R.string.item_ringtone_no_file
        override val isUserAdjustable: Boolean = true
        override fun getLabelRes(): Int = labelRes

        companion object {
            const val labelRes: Int = R.string.label_system_ringtone
        }
    }

    class File() : AudioItem(ID.CUSTOM_AUDIO) {

        constructor(context: Context) : this() {
            displayText = getDisplayText(context, null)
        }

        override val fallbackStringRes: Int = R.string.item_audio_no_file
        override val isUserAdjustable: Boolean = true
        override fun getLabelRes(): Int = labelRes

        companion object {
            const val labelRes: Int = R.string.label_custom_audio
        }
    }
}
