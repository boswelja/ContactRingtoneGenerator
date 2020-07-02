package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.R

class TextItem : StructureItem(ID.TEXT_ITEM) {

    override val isDynamic: Boolean = true

    var text: String = ""

    override fun getLabelRes(): Int = labelRes
    override fun getEngineText(): String = text

    companion object {
        const val labelRes: Int = R.string.label_custom_text
    }
}
