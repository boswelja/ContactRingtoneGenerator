package com.boswelja.contactringtonegenerator.ringtonegen.item

class FirstName : BaseItem(ID.FIRST_NAME) {

    override fun getLabel(): String {
        return "Given Name"
    }

    override fun getEngineText(): String = Constants.FIRST_NAME_PLACEHOLDER
}
