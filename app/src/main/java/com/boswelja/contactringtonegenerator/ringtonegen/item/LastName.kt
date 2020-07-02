package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.R

class LastName : BaseNameItem(ID.LAST_NAME) {

    override fun getLabelRes(): Int = labelRes
    override fun getEngineText(): String = Constants.LAST_NAME_PLACEHOLDER

    companion object {
        const val labelRes: Int = R.string.label_last_name
    }
}
