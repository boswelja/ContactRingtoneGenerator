package com.boswelja.contactringtonegenerator.ringtonegen.item

class Nickname : BaseItem(ID.PREFIX) {

    override fun getLabel(): String {
        return "Nickname"
    }

    override fun getEngineText(): String = Constants.NAME_PREFIX_PLACEHOLDER
}
