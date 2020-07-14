package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem

class SystemRingtone : AudioItem(ID.SYSTEM_RINGTONE) {

    override val isUserAdjustable: Boolean = true
    override fun getLabelRes(): Int = labelRes

    companion object {
        const val labelRes: Int = R.string.label_system_ringtone
    }
}
