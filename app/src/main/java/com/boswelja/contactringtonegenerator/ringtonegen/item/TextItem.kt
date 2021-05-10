package com.boswelja.contactringtonegenerator.ringtonegen.item

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import com.boswelja.contactringtonegenerator.R

sealed class TextItem(id: ID) : StructureItem(id) {
    abstract val engineString: String
    override val icon = Icons.Filled.TextFields

    class Custom : TextItem(ID.CUSTOM_TEXT) {
        var text: String = ""
        override val labelRes: Int = R.string.label_custom_text
        override val engineString = text
        override val isDataValid: Boolean
            get() = text.isNotBlank()
    }

    class NamePrefix : TextItem(ID.PREFIX) {
        override val labelRes: Int = R.string.label_name_prefix
        override val engineString = Constants.NAME_PREFIX_PLACEHOLDER
        override val isDataValid = true
    }

    class FirstName : TextItem(ID.FIRST_NAME) {
        override val labelRes: Int = R.string.label_first_name
        override val engineString = Constants.FIRST_NAME_PLACEHOLDER
        override val isDataValid = true
    }

    class MiddleName : TextItem(ID.MIDDLE_NAME) {
        override val labelRes: Int = R.string.label_middle_name
        override val engineString = Constants.MIDDLE_NAME_PLACEHOLDER
        override val isDataValid = true
    }

    class LastName : TextItem(ID.LAST_NAME) {
        override val labelRes: Int = R.string.label_last_name
        override val engineString = Constants.LAST_NAME_PLACEHOLDER
        override val isDataValid = true
    }

    class NameSuffix : TextItem(ID.SUFFIX) {
        override val labelRes: Int = R.string.label_name_suffix
        override val engineString = Constants.NAME_SUFFIX_PLACEHOLDER
        override val isDataValid = true
    }

    class Nickname : TextItem(ID.PREFIX) {
        override val labelRes = R.string.label_nickname
        override val engineString = Constants.NAME_PREFIX_PLACEHOLDER
        override val isDataValid = true
    }
}
