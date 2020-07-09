package com.boswelja.contactringtonegenerator.ringtonegen.item.common

import com.boswelja.contactringtonegenerator.ringtonegen.item.ID

abstract class NameItem(id: ID) : TextItem(id) {
    override val isUserAdjustable: Boolean = false
}
