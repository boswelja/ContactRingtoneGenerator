package com.boswelja.contactringtonegenerator.ringtonegen.item.common

import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID
import java.io.File

abstract class AudioItem(id: ID) : StructureItem(id) {

    override val isUserAdjustable: Boolean = true

    abstract fun getAudioFile(): File?
    override fun getIconRes(): Int = R.drawable.structure_ic_text
}