package com.boswelja.contactringtonegenerator.ringtonegen.item

class LastName : BaseNameItem(ID.LAST_NAME) {

    override fun getLabel(): String = label
    override fun getEngineText(): String = Constants.LAST_NAME_PLACEHOLDER

    companion object {
        const val label: String = "Family Name"
    }
}
