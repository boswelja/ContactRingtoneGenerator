package com.boswelja.contactringtonegenerator.ringtonegen.item

class TextItem : BaseItem(ID.TEXT_ITEM) {

    override val isDynamic: Boolean = false

    var text: String = ""

    override fun getLabel(): String {
        return "Custom Text"
    }

    override fun getEngineText(): String = text
}