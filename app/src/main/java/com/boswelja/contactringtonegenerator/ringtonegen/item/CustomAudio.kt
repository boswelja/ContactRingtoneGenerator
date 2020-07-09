package com.boswelja.contactringtonegenerator.ringtonegen.item

import android.net.Uri
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem

class CustomAudio : AudioItem(ID.CUSTOM_AUDIO) {

    var audioUri: Uri? = null

    override val isUserAdjustable: Boolean = true
    override fun getAudioContentUri(): Uri? = audioUri
    override fun getLabelRes(): Int = labelRes

    companion object {
        const val labelRes: Int = R.string.label_custom_audio
    }
}
