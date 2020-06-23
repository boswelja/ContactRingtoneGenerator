package com.boswelja.contactringtonegenerator.ringtonegen.item

abstract class BaseItem(val id: ID) {

    abstract fun getLabel(): String
    abstract fun getEngineText(): String

}
