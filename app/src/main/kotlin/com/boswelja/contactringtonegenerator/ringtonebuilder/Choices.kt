package com.boswelja.contactringtonegenerator.ringtonebuilder

import androidx.annotation.StringRes
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem

abstract class Choice(
    @StringRes val textRes: Int
) {
    abstract fun createStructureItem(): StructureItem<*>
}

class CustomText : Choice(R.string.label_custom_text) {
    override fun createStructureItem() = StructureItem.Text.CustomText()
}

class NamePrefix : Choice(R.string.label_name_prefix) {
    override fun createStructureItem() = StructureItem.Text.NamePrefix()
}

class FirstName : Choice(R.string.label_first_name) {
    override fun createStructureItem() = StructureItem.Text.FirstName()
}

class MiddleName : Choice(R.string.label_middle_name) {
    override fun createStructureItem() = StructureItem.Text.MiddleName()
}

class LastName : Choice(R.string.label_last_name) {
    override fun createStructureItem() = StructureItem.Text.LastName()
}

class NameSuffix : Choice(R.string.label_name_suffix) {
    override fun createStructureItem() = StructureItem.Text.NameSuffix()
}

class Nickname : Choice(R.string.label_nickname) {
    override fun createStructureItem() = StructureItem.Text.Nickname()
}

class CustomAudio : Choice(R.string.label_custom_audio) {
    override fun createStructureItem() = StructureItem.Audio.AudioFile()
}

class SystemRingtone : Choice(R.string.label_system_ringtone) {
    override fun createStructureItem() = StructureItem.Audio.SystemRingtone()
}
