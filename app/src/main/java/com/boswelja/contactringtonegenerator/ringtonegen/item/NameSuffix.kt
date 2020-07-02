package com.boswelja.contactringtonegenerator.ringtonegen.item

class NameSuffix : BaseNameItem(ID.SUFFIX) {

    override fun getLabel(): String {
        return "Name Suffix"
    }

    override fun getEngineText(): String = Constants.NAME_SUFFIX_PLACEHOLDER
}
