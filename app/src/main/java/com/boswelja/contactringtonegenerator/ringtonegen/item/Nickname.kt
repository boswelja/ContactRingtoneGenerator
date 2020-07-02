package com.boswelja.contactringtonegenerator.ringtonegen.item

class Nickname : BaseNameItem(ID.PREFIX) {

    override fun getLabel(): String {
        return "Nickname"
    }

    override fun getEngineText(): String = Constants.NAME_PREFIX_PLACEHOLDER
}
