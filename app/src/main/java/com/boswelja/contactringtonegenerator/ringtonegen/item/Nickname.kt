package com.boswelja.contactringtonegenerator.ringtonegen.item

class Nickname : BaseNameItem(ID.PREFIX) {

    override fun getLabel(): String = label
    override fun getEngineText(): String = Constants.NAME_PREFIX_PLACEHOLDER

    companion object {
        const val label: String = "Nickname"
    }
}
