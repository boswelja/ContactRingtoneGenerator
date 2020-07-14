package com.boswelja.contactringtonegenerator.ringtonegen.item

import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

sealed class TextItem(id: ID) : StructureItem(id) {
    abstract fun getEngineText(): String
    override fun getIconRes(): Int = iconRes

    companion object {
        const val iconRes: Int = R.drawable.structure_ic_text
    }

    class Custom : TextItem(ID.CUSTOM_TEXT) {
        override val isUserAdjustable: Boolean = true
        override fun getLabelRes(): Int = labelRes
        override fun getEngineText(): String = text

        var text: String = ""

        companion object {
            const val labelRes: Int = R.string.label_custom_text
        }
    }

    class NamePrefix : TextItem(ID.PREFIX) {
        override val isUserAdjustable: Boolean = false
        override fun getLabelRes(): Int = labelRes
        override fun getEngineText(): String = Constants.NAME_PREFIX_PLACEHOLDER

        companion object {
            const val labelRes: Int = R.string.label_name_prefix
        }
    }

    class FirstName : TextItem(ID.FIRST_NAME) {
        override val isUserAdjustable: Boolean = false
        override fun getLabelRes(): Int = labelRes
        override fun getEngineText(): String = Constants.FIRST_NAME_PLACEHOLDER

        companion object {
            const val labelRes: Int = R.string.label_first_name
        }
    }

    class MiddleName : TextItem(ID.MIDDLE_NAME) {
        override val isUserAdjustable: Boolean = false
        override fun getLabelRes(): Int = labelRes
        override fun getEngineText(): String = Constants.MIDDLE_NAME_PLACEHOLDER

        companion object {
            const val labelRes: Int = R.string.label_middle_name
        }
    }

    class LastName : TextItem(ID.LAST_NAME) {
        override val isUserAdjustable: Boolean = false
        override fun getLabelRes(): Int = labelRes
        override fun getEngineText(): String = Constants.LAST_NAME_PLACEHOLDER

        companion object {
            const val labelRes: Int = R.string.label_last_name
        }
    }

    class NameSuffix : TextItem(ID.SUFFIX) {
        override val isUserAdjustable: Boolean = false
        override fun getLabelRes(): Int = labelRes
        override fun getEngineText(): String = Constants.NAME_SUFFIX_PLACEHOLDER

        companion object {
            const val labelRes: Int = R.string.label_name_suffix
        }
    }

    class Nickname : TextItem(ID.PREFIX) {
        override val isUserAdjustable: Boolean = false
        override fun getLabelRes(): Int = labelRes
        override fun getEngineText(): String = Constants.NAME_PREFIX_PLACEHOLDER

        companion object {
            const val labelRes: Int = R.string.label_nickname
        }
    }
}