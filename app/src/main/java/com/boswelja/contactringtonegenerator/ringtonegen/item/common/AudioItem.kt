package com.boswelja.contactringtonegenerator.ringtonegen.item.common

import android.net.Uri
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID

abstract class AudioItem(id: ID) : StructureItem(id) {

    override val isUserAdjustable: Boolean = true

    abstract fun getAudioContentUri(): Uri?
    override fun getIconRes(): Int = iconRes

    companion object {
        const val iconRes: Int = R.drawable.structure_ic_audio
    }
}
