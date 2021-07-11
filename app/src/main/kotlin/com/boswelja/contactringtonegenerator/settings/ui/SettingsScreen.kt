package com.boswelja.contactringtonegenerator.settings.ui

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
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
                launchTtsSettings(context)
            }
        ) {
            Text(stringResource(R.string.launch_tts_settings_title))
        }
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

private fun launchTtsSettings(context: Context) {
    Intent().apply {
        action = "com.android.settings.TTS_SETTINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }.also {
        context.startActivity(it)
    }
}
