package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.TextItem

class CustomText : TextItem(ID.CUSTOM_TEXT) {

    override val isUserAdjustable: Boolean = true

    var text: String = ""

    override fun getLabelRes(): Int = labelRes
    override fun getEngineText(): String = text

    companion object {
        const val labelRes: Int = R.string.label_custom_text
    }
}
