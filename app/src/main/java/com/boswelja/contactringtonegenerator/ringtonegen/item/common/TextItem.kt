package com.boswelja.contactringtonegenerator.ringtonegen.item.common

import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID

abstract class TextItem(id: ID) : StructureItem(id) {
    abstract fun getEngineText(): String
    override fun getIconRes(): Int = R.drawable.structure_ic_text
}