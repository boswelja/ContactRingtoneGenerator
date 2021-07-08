package com.boswelja.contactringtonegenerator.ringtonegen.item

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

sealed class CustomAudioItem : StructureItem() {
    final override var data: String? = null
    final override val isDataValid: Boolean
        get() = audioUri != null

    val audioUri: Uri?
        get() = data?.let { Uri.parse(it) }

    class SystemRingtone : CustomAudioItem()
    class AudioFile : CustomAudioItem()
}

sealed class CustomTextItem : StructureItem() {
    final override var data: String? by mutableStateOf("")
    final override val isDataValid: Boolean
        get() = !data.isNullOrBlank()

    class CustomText : CustomTextItem()
}

sealed class ContactDataItem(
    final override var data: String?
) : StructureItem() {

    final override val isDataValid: Boolean = true

    class NamePrefix : ContactDataItem(Constants.NAME_PREFIX_PLACEHOLDER)
    class FirstName : ContactDataItem(Constants.FIRST_NAME_PLACEHOLDER)
    class MiddleName : ContactDataItem(Constants.MIDDLE_NAME_PLACEHOLDER)
    class LastName : ContactDataItem(Constants.LAST_NAME_PLACEHOLDER)
    class NameSuffix : ContactDataItem(Constants.NAME_SUFFIX_PLACEHOLDER)
    class Nickname : ContactDataItem(Constants.NICKNAME_PLACEHOLDER)
}
