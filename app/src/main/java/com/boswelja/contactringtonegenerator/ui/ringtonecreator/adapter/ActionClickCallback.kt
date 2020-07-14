package com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter

import com.boswelja.contactringtonegenerator.ringtonegen.item.ID
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

class ActionClickCallback(val callback: (id: ID, position: Int) -> Unit) {
    fun onClick(item: StructureItem, position: Int) = callback(item.id, position)
}