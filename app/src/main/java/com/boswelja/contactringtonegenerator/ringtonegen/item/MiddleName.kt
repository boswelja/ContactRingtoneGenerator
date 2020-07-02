package com.boswelja.contactringtonegenerator.ringtonegen.item

class MiddleName : BaseItem(ID.MIDDLE_NAME) {

    override fun getLabel(): String {
        return "Middle Name"
    }

    override fun getEngineText(): String = Constants.MIDDLE_NAME_PLACEHOLDER
}
