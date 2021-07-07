package com.boswelja.contactringtonegenerator.ringtonebuilder

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import com.boswelja.contactringtonegenerator.R

abstract class ChoiceCategory(
    val icon: ImageVector,
    @StringRes val textRes: Int,
    val choices: List<Choice>
)

val AllCategories = listOf(
    ContactDataCategory,
    CustomTextCategory,
    CustomAudioCategory
)

object ContactDataCategory : ChoiceCategory(
    Icons.Default.ContactPage,
    R.string.choice_category_contact_data,
    listOf(
        Nickname(),
        NamePrefix(),
        FirstName(),
        MiddleName(),
        LastName(),
        NameSuffix()
    )
)

object CustomTextCategory : ChoiceCategory(
    Icons.Default.TextFields,
    R.string.choice_category_custom_text,
    listOf(
        CustomText()
    )
)

object CustomAudioCategory : ChoiceCategory(
    Icons.Default.Audiotrack,
    R.string.choice_category_custom_audio,
    listOf(
        CustomAudio(),
        SystemRingtone()
    )
)
