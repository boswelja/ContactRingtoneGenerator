package com.boswelja.contactringtonegenerator.ringtonegen.item.common

import android.net.Uri
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID

abstract class AudioItem(id: ID) : StructureItem(id) {

    var audioUri: Uri? = null

    override val isUserAdjustable: Boolean = true

    override fun getIconRes(): Int = iconRes

    companion object {
        const val iconRes: Int = R.drawable.structure_ic_audio
    }
}
