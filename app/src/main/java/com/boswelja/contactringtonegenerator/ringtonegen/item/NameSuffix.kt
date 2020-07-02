package com.boswelja.contactringtonegenerator.ringtonegen.item

class NameSuffix : BaseNameItem(ID.SUFFIX) {

    override fun getLabel(): String = label
    override fun getEngineText(): String = Constants.NAME_SUFFIX_PLACEHOLDER

    companion object {
        const val label: String = "Name Suffix"
    }
}
