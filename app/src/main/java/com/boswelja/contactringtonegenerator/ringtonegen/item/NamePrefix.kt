package com.boswelja.contactringtonegenerator.ringtonegen.item

class NamePrefix : BaseNameItem(ID.PREFIX) {

    override fun getLabel(): String {
        return "Name Prefix"
    }

    override fun getEngineText(): String = Constants.NAME_PREFIX_PLACEHOLDER
}
