package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.R

class MiddleName : BaseNameItem(ID.MIDDLE_NAME) {

    override fun getLabelRes(): Int = labelRes
    override fun getEngineText(): String = Constants.MIDDLE_NAME_PLACEHOLDER

    companion object {
        const val labelRes: Int = R.string.label_middle_name
    }
}
