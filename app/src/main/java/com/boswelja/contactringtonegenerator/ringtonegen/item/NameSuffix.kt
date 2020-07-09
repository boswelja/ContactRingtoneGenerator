package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.NameItem

class NameSuffix : NameItem(ID.SUFFIX) {

    override fun getLabelRes(): Int = labelRes
    override fun getEngineText(): String = Constants.NAME_SUFFIX_PLACEHOLDER

    companion object {
        const val labelRes: Int = R.string.label_name_suffix
    }
}
