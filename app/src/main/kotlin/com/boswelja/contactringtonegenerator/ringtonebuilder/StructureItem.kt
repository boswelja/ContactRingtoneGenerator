package com.boswelja.contactringtonegenerator.ringtonebuilder

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.Constants
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.Serializable

private val idCounter = AtomicInteger()

@Serializable
sealed class StructureItem {
    val id: Int = idCounter.incrementAndGet()
    abstract val isDataValid: Boolean
    abstract var data: String?

    override fun equals(other: Any?): Boolean {
        if (other !is StructureItem)
            return false
        return other.id == id
    }

    override fun hashCode(): Int = id
}

@Serializable
sealed class CustomAudioItem : StructureItem() {
    final override var data: String? = null
    final override val isDataValid: Boolean
        get() = audioUri != null

    val audioUri: Uri?
        get() = data?.let { Uri.parse(it) }

    @Serializable
    class SystemRingtone : CustomAudioItem()
    @Serializable
    class AudioFile : CustomAudioItem()
}

@Serializable
sealed class CustomTextItem : StructureItem() {
    final override var data: String? by mutableStateOf("")
    final override val isDataValid: Boolean
        get() = !data.isNullOrBlank()

    @Serializable
    class CustomText : CustomTextItem()
}

@Serializable
sealed class ContactDataItem(
    final override var data: String?,
    @StringRes val textRes: Int
) : StructureItem() {

    final override val isDataValid: Boolean = true

    @Serializable
    class NamePrefix : ContactDataItem(
        Constants.NAME_PREFIX_PLACEHOLDER,
        R.string.label_name_prefix
    )

    @Serializable
    class FirstName : ContactDataItem(
        Constants.FIRST_NAME_PLACEHOLDER,
        R.string.label_first_name
    )

    @Serializable
    class MiddleName : ContactDataItem(
        Constants.MIDDLE_NAME_PLACEHOLDER,
        R.string.label_middle_name
    )

    @Serializable
    class LastName : ContactDataItem(
        Constants.LAST_NAME_PLACEHOLDER,
        R.string.label_last_name
    )

    @Serializable
    class NameSuffix : ContactDataItem(
        Constants.NAME_SUFFIX_PLACEHOLDER,
        R.string.label_name_suffix
    )

    @Serializable
    class Nickname : ContactDataItem(
        Constants.NICKNAME_PLACEHOLDER,
        R.string.label_nickname
    )
}
