package com.boswelja.contactringtonegenerator.ringtonebuilder

import androidx.annotation.StringRes
import com.boswelja.contactringtonegenerator.R

abstract class Choice(
    @StringRes val textRes: Int
) {
    abstract fun createStructureItem(): StructureItem
}

class CustomText : Choice(R.string.label_custom_text) {
    override fun createStructureItem() = CustomTextItem.CustomText()
}

class NamePrefix : Choice(R.string.label_name_prefix) {
    override fun createStructureItem() = ContactDataItem.NamePrefix()
}

class FirstName : Choice(R.string.label_first_name) {
    override fun createStructureItem() = ContactDataItem.FirstName()
}

class MiddleName : Choice(R.string.label_middle_name) {
    override fun createStructureItem() = ContactDataItem.MiddleName()
}

class LastName : Choice(R.string.label_last_name) {
    override fun createStructureItem() = ContactDataItem.LastName()
}

class NameSuffix : Choice(R.string.label_name_suffix) {
    override fun createStructureItem() = ContactDataItem.NameSuffix()
}

class Nickname : Choice(R.string.label_nickname) {
    override fun createStructureItem() = ContactDataItem.Nickname()
}

class CustomAudio : Choice(R.string.label_custom_audio) {
    override fun createStructureItem() = CustomAudioItem.AudioFile()
}

class SystemRingtone : Choice(R.string.label_system_ringtone) {
    override fun createStructureItem() = CustomAudioItem.SystemRingtone()
}
