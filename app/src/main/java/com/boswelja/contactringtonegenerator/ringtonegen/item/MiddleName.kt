package com.boswelja.contactringtonegenerator.ringtonegen.item

class MiddleName : BaseNameItem(ID.MIDDLE_NAME) {

    override fun getLabel(): String = label
    override fun getEngineText(): String = Constants.MIDDLE_NAME_PLACEHOLDER

    companion object {
        const val label: String = "Middle Name"
    }
}
