package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem
import java.io.File

class CustomAudio : AudioItem(ID.CUSTOM_AUDIO) {

    var file: File? = null

    override val isUserAdjustable: Boolean = true
    override fun getAudioFile(): File? = file

    override fun getLabelRes(): Int {
        TODO("Not yet implemented")
    }
}