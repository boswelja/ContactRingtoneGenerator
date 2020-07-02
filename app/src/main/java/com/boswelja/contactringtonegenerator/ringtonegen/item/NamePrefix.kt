package com.boswelja.contactringtonegenerator.ringtonegen.item

class NamePrefix : BaseItem(ID.PREFIX) {

    override fun getLabel(): String {
        return "Name Prefix"
    }

    override fun getEngineText(): String = Constants.NAME_PREFIX_PLACEHOLDER
}
