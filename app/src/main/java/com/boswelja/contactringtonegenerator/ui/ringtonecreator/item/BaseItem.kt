package com.boswelja.contactringtonegenerator.ui.ringtonecreator.item

abstract class BaseItem(val id: ID) {

    abstract fun getLabel(): String
    abstract fun getEngineText(): String
}
