package com.boswelja.contactringtonegenerator.ringtonegen.item

abstract class BaseItem(val id: ID) {

    abstract fun getLabel(): String
    abstract fun getEngineText(): String

    override fun equals(other: Any?): Boolean {
        if (other is BaseItem) {
            return other.id == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
