package com.boswelja.contactringtonegenerator.ringtonegen.item

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import com.boswelja.contactringtonegenerator.R
import java.lang.IllegalArgumentException

sealed class StructureItem<out T> {

    abstract val dataType: DataType
    abstract val isDataValid: Boolean
    abstract val icon: ImageVector
    abstract val labelRes: Int
    abstract val engineRepresentation: String
    abstract val data: T

    abstract fun setData(newData: Any)

    enum class DataType {
        IMMUTABLE,
        AUDIO_FILE,
        SYSTEM_RINGTONE,
        CUSTOM_TEXT
    }

    class CustomText : StructureItem<String>() {
        private var text = ""
        override val data: String
            get() = text
        override fun setData(newData: Any) {
            if (newData !is String) throw IllegalArgumentException("Expected String")
            text = newData
        }
        override val dataType = DataType.CUSTOM_TEXT
        override val engineRepresentation
            get() = data
        override val isDataValid: Boolean
            get() = data.isBlank()

        override val labelRes: Int = R.string.label_custom_text
        override val icon = Icons.Default.TextFields
    }

    class NamePrefix : StructureItem<String?>() {
        override val data: String? = null
        override fun setData(newData: Any) { }
        override val dataType = DataType.IMMUTABLE
        override val engineRepresentation = Constants.NAME_PREFIX_PLACEHOLDER
        override val isDataValid = true

        override val labelRes: Int = R.string.label_name_prefix
        override val icon = Icons.Default.ContactPage
    }

    class FirstName : StructureItem<String?>() {
        override var data: String? = null
        override fun setData(newData: Any) { }
        override val dataType = DataType.IMMUTABLE
        override val engineRepresentation = Constants.FIRST_NAME_PLACEHOLDER
        override val isDataValid = true

        override val labelRes: Int = R.string.label_first_name
        override val icon = Icons.Default.ContactPage
    }

    class MiddleName : StructureItem<String?>() {
        override var data: String? = null
        override fun setData(newData: Any) { }
        override val dataType = DataType.IMMUTABLE
        override val engineRepresentation = Constants.MIDDLE_NAME_PLACEHOLDER
        override val isDataValid = true

        override val labelRes: Int = R.string.label_middle_name
        override val icon = Icons.Default.ContactPage
    }

    class LastName : StructureItem<String?>() {
        override var data: String? = null
        override fun setData(newData: Any) { }
        override val dataType = DataType.IMMUTABLE
        override val engineRepresentation = Constants.LAST_NAME_PLACEHOLDER
        override val isDataValid = true

        override val labelRes: Int = R.string.label_last_name
        override val icon = Icons.Default.ContactPage
    }

    class NameSuffix : StructureItem<String?>() {
        override var data: String? = null
        override fun setData(newData: Any) { }
        override val dataType = DataType.IMMUTABLE
        override val engineRepresentation = Constants.NAME_SUFFIX_PLACEHOLDER
        override val isDataValid = true

        override val labelRes: Int = R.string.label_name_suffix
        override val icon = Icons.Default.ContactPage
    }

    class Nickname : StructureItem<String?>() {
        override var data: String? = null
        override fun setData(newData: Any) { }
        override val dataType = DataType.IMMUTABLE
        override val engineRepresentation = Constants.NICKNAME_PLACEHOLDER
        override val isDataValid = true

        override val labelRes: Int = R.string.label_nickname
        override val icon = Icons.Default.ContactPage
    }

    class SystemRingtone : StructureItem<Uri?>() {
        override var data: Uri? = null
        override fun setData(newData: Any) { }
        override val engineRepresentation: String = ""
        override val dataType = DataType.SYSTEM_RINGTONE
        override val isDataValid: Boolean
            get() = data != null

        override val icon = Icons.Default.Audiotrack
        override val labelRes = R.string.label_system_ringtone
    }

    class AudioFile : StructureItem<Uri?>() {
        override var data: Uri? = null
        override fun setData(newData: Any) { }
        override val engineRepresentation: String = ""
        override val dataType = DataType.AUDIO_FILE
        override val isDataValid: Boolean
            get() = data != null

        override val icon = Icons.Default.Audiotrack
        override val labelRes = R.string.label_custom_audio
    }
}
