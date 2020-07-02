package com.boswelja.contactringtonegenerator.ringtonegen.item

class FirstName : BaseNameItem(ID.FIRST_NAME) {

    override fun getLabel(): String = label

    override fun getEngineText(): String = Constants.FIRST_NAME_PLACEHOLDER

    companion object {
        const val label: String = "Given Name"
    }
}
