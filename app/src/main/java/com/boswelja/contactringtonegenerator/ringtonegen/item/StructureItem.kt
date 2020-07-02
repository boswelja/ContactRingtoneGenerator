package com.boswelja.contactringtonegenerator.ringtonegen.item

abstract class StructureItem(val id: ID) {

    /**
     * Indicates whether this [StructureItem] is a user-adjustable item.
     */
    abstract val isDynamic: Boolean

    abstract fun getLabelRes(): Int
    abstract fun getEngineText(): String

    override fun equals(other: Any?): Boolean {
        if (other is StructureItem) {
            return other.id == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
