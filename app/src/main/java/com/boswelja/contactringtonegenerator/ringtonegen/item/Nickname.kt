package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.R

class Nickname : BaseNameItem(ID.PREFIX) {

    override fun getLabelRes(): Int = labelRes
    override fun getEngineText(): String = Constants.NAME_PREFIX_PLACEHOLDER

    companion object {
        const val labelRes: Int = R.string.label_nickname
    }
}
