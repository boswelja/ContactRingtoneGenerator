package com.boswelja.contactringtonegenerator.ringtonegen.item.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.TextItem

sealed class StructureChoice(
    val icon: ImageVector,
    @StringRes val textRes: Int
) {
    abstract fun createStructureItem(): StructureItem

    class CustomText : StructureChoice(
        Icons.Outlined.TextFields,
        R.string.label_custom_text
    ) {
        override fun createStructureItem(): StructureItem = TextItem.Custom()
    }

    class NamePrefix : StructureChoice(
        Icons.Outlined.TextFields,
        R.string.label_name_prefix
    ) {
        override fun createStructureItem(): StructureItem = TextItem.NamePrefix()
    }

    class FirstName : StructureChoice(
        Icons.Outlined.TextFields,
        R.string.label_first_name
    ) {
        override fun createStructureItem(): StructureItem = TextItem.FirstName()
    }

    class MiddleName : StructureChoice(
        Icons.Outlined.TextFields,
        R.string.label_middle_name
    ) {
        override fun createStructureItem(): StructureItem = TextItem.MiddleName()
    }

    class LastName : StructureChoice(
        Icons.Outlined.TextFields,
        R.string.label_last_name
    ) {
        override fun createStructureItem(): StructureItem = TextItem.LastName()
    }

    class NameSuffix : StructureChoice(
        Icons.Outlined.TextFields,
        R.string.label_name_suffix
    ) {
        override fun createStructureItem(): StructureItem = TextItem.NameSuffix()
    }

    class Nickname : StructureChoice(
        Icons.Outlined.TextFields,
        R.string.label_nickname
    ) {
        override fun createStructureItem(): StructureItem = TextItem.Nickname()
    }

    class CustomAudio : StructureChoice(
        Icons.Outlined.Audiotrack,
        R.string.label_custom_audio
    ) {
        override fun createStructureItem(): StructureItem = AudioItem.File()
    }

    class SystemRingtone : StructureChoice(
        Icons.Outlined.Audiotrack,
        R.string.label_system_ringtone
    ) {
        override fun createStructureItem(): StructureItem = AudioItem.SystemRingtone()
    }

    companion object {
        val ALL = arrayOf(
            CustomText(),
            NamePrefix(),
            FirstName(),
            MiddleName(),
            LastName(),
            NameSuffix(),
            Nickname(),
            CustomAudio(),
            SystemRingtone()
        )
    }
}
