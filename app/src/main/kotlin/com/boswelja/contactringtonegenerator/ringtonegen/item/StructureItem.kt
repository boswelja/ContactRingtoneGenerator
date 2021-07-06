package com.boswelja.contactringtonegenerator.ringtonegen.item

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import com.boswelja.contactringtonegenerator.R

sealed class StructureItem<T> {

    abstract val dataType: DataType
    abstract val isDataValid: Boolean
    abstract val icon: ImageVector
    abstract val labelRes: Int
    abstract val engineRepresentation: String
    abstract var data: T

    enum class DataType {
        DYNAMIC,
        AUDIO_FILE,
        SYSTEM_RINGTONE,
        CUSTOM_TEXT
    }

    sealed class Text : StructureItem<String>() {

        override var data: String = ""
        override val isDataValid = true

        class CustomText : Text() {
            override val dataType = DataType.CUSTOM_TEXT
            override val engineRepresentation
                get() = data
            override val isDataValid: Boolean
                get() = data.isNotBlank()

            override val labelRes: Int = R.string.label_custom_text
            override val icon = Icons.Default.TextFields
        }

        class NamePrefix : Text() {
            override val dataType = DataType.DYNAMIC
            override val engineRepresentation = Constants.NAME_PREFIX_PLACEHOLDER

            override val labelRes: Int = R.string.label_name_prefix
            override val icon = Icons.Default.ContactPage
        }

        class FirstName : Text() {
            override val dataType = DataType.DYNAMIC
            override val engineRepresentation = Constants.FIRST_NAME_PLACEHOLDER

            override val labelRes: Int = R.string.label_first_name
            override val icon = Icons.Default.ContactPage
        }

        class MiddleName : Text() {
            override val dataType = DataType.DYNAMIC
            override val engineRepresentation = Constants.MIDDLE_NAME_PLACEHOLDER

            override val labelRes: Int = R.string.label_middle_name
            override val icon = Icons.Default.ContactPage
        }

        class LastName : Text() {
            override val dataType = DataType.DYNAMIC
            override val engineRepresentation = Constants.LAST_NAME_PLACEHOLDER

            override val labelRes: Int = R.string.label_last_name
            override val icon = Icons.Default.ContactPage
        }

        class NameSuffix : Text() {
            override val dataType = DataType.DYNAMIC
            override val engineRepresentation = Constants.NAME_SUFFIX_PLACEHOLDER

            override val labelRes: Int = R.string.label_name_suffix
            override val icon = Icons.Default.ContactPage
        }

        class Nickname : Text() {
            override val dataType = DataType.DYNAMIC
            override val engineRepresentation = Constants.NICKNAME_PLACEHOLDER

            override val labelRes: Int = R.string.label_nickname
            override val icon = Icons.Default.ContactPage
        }
    }

    sealed class Audio : StructureItem<Uri?>() {

        override var data: Uri? = null
        override val isDataValid: Boolean
            get() = data != null
        override val engineRepresentation: String
            get() = data?.toString() ?: ""

        class SystemRingtone : Audio() {
            override val dataType = DataType.SYSTEM_RINGTONE
            override val icon = Icons.Default.Audiotrack
            override val labelRes = R.string.label_system_ringtone
        }

        class AudioFile : Audio() {
            override val dataType = DataType.AUDIO_FILE
            override val icon = Icons.Default.Audiotrack
            override val labelRes = R.string.label_custom_audio
        }
    }
}
