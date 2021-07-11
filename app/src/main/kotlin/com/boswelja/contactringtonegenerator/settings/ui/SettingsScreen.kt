package com.boswelja.contactringtonegenerator.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonDefaults.outlinedButtonColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.common.ui.SliderSetting
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: SettingsViewModel = viewModel()

    Column(modifier) {
        VolumeMultiplierSetting(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            value = viewModel.volumeMultiplier,
            onSliderValueChanged = { viewModel.volumeMultiplier = it },
            onSliderValueFinished = { viewModel.saveVolumeMultiplier(it) }
        )
        Divider(
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.12f)
        )
        TtsSettings(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Divider(
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.12f)
        )
        ButtonSettings(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            viewModel = viewModel
        )
    }
}

@ExperimentalCoroutinesApi
@Composable
fun ButtonSettings(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val colors = outlinedButtonColors(
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.primary,
        disabledContentColor = MaterialTheme.colors.onBackground
            .copy(alpha = ContentAlpha.disabled)
    )
    val border = BorderStroke(
        ButtonDefaults.OutlinedBorderSize,
        MaterialTheme.colors.onBackground.copy(alpha = ButtonDefaults.OutlinedBorderOpacity)
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            colors = colors,
            border = border,
            onClick = {
                viewModel.resetContactRingtones()
            }
        ) {
            Text(stringResource(R.string.reset_ringtones_title))
        }
    }
}

@Composable
fun VolumeMultiplierSetting(
    modifier: Modifier = Modifier,
    value: Float,
    onSliderValueChanged: (Float) -> Unit,
    onSliderValueFinished: (Float) -> Unit
) {
    SliderSetting(
        modifier = modifier,
        value = value,
        valueRange = 1f..2f,
        text = {
            Text(stringResource(R.string.volume_boost_title))
        },
        valueText = {
            Text("%.1fx".format(value))
        },
        onValueChanged = onSliderValueChanged,
        onValueChangeFinished = onSliderValueFinished
    )
}

@Composable
fun TtsSettings(
    modifier: Modifier = Modifier
) {
    val viewModel: SettingsViewModel = viewModel()

    Column(modifier) {
        TtsVoicePitchSetting(
            value = viewModel.ttsVoicePitch,
            onSliderValueChanged = { viewModel.ttsVoicePitch = it },
            onSliderValueFinished = { viewModel.saveTtsVoicePitch(it) }
        )
        TtsSpeechRateSetting(
            value = viewModel.ttsSpeechRate,
            onSliderValueChanged = { viewModel.ttsSpeechRate = it },
            onSliderValueFinished = { viewModel.saveTtsSpeechRate(it) }
        )
    }
}

@Composable
fun TtsVoicePitchSetting(
    modifier: Modifier = Modifier,
    value: Float,
    onSliderValueChanged: (Float) -> Unit,
    onSliderValueFinished: (Float) -> Unit
) {
    SliderSetting(
        modifier = modifier,
        value = value,
        valueRange = 0.5f..2f,
        text = {
            Text(stringResource(R.string.tts_setting_pitch))
        },
        valueText = {
            Text("%.1fx".format(value))
        },
        onValueChanged = onSliderValueChanged,
        onValueChangeFinished = onSliderValueFinished
    )
}

@Composable
fun TtsSpeechRateSetting(
    modifier: Modifier = Modifier,
    value: Float,
    onSliderValueChanged: (Float) -> Unit,
    onSliderValueFinished: (Float) -> Unit
) {
    SliderSetting(
        modifier = modifier,
        value = value,
        valueRange = 0.5f..2f,
        text = {
            Text(stringResource(R.string.tts_setting_rate))
        },
        valueText = {
            Text("%.1fx".format(value))
        },
        onValueChanged = onSliderValueChanged,
        onValueChangeFinished = onSliderValueFinished
    )
}
