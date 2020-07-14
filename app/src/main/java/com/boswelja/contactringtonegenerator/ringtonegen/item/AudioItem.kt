package com.boswelja.contactringtonegenerator.ringtonegen.item

import android.net.Uri
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

sealed class AudioItem(id: ID) : StructureItem(id) {

    var audioUri: Uri? = null

    override val isUserAdjustable: Boolean = true

    override fun getIconRes(): Int = iconRes

    companion object {
        const val iconRes: Int = R.drawable.structure_ic_audio
    }

    class SystemRingtone : AudioItem(ID.SYSTEM_RINGTONE) {

        override val isUserAdjustable: Boolean = true
        override fun getLabelRes(): Int = labelRes

        companion object {
            const val labelRes: Int = R.string.label_system_ringtone
        }
    }

    class File : AudioItem(ID.CUSTOM_AUDIO) {

        override val isUserAdjustable: Boolean = true
        override fun getLabelRes(): Int = labelRes

        companion object {
            const val labelRes: Int = R.string.label_custom_audio
        }
    }
}
