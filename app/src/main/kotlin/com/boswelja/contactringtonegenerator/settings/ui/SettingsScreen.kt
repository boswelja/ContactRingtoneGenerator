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
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.contactringtonegenerator.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: SettingsViewModel = viewModel()
    var volumeMultiplier by remember {
        mutableStateOf(1.0f)
    }
    LaunchedEffect("initialVolumeMultiplierValue") {
        volumeMultiplier = viewModel.volumeMultiplier.first()
    }

    Column(modifier) {
        VolumeMultiplierSetting(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            value = volumeMultiplier,
            onSliderValueChanged = { volumeMultiplier = it },
            onSliderValueFinished = { viewModel.setVolumeMultiplier(it) }
        )
        Divider(
            color = MaterialTheme.colors.onPrimary.copy(alpha = 0.12f)
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
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        disabledContentColor = MaterialTheme.colors.onPrimary
    )
    val border = BorderStroke(
        ButtonDefaults.OutlinedBorderSize,
        MaterialTheme.colors.onPrimary.copy(alpha = ButtonDefaults.OutlinedBorderOpacity)
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
    val colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colors.onPrimary,
        activeTrackColor = MaterialTheme.colors.onPrimary
    )
    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.volume_boost_title),
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = "%.1fx".format(value),
                style = MaterialTheme.typography.body2
            )
        }
        Slider(
            value = value,
            valueRange = 0.5f..2f,
            onValueChange = onSliderValueChanged,
            onValueChangeFinished = { onSliderValueFinished(value) },
            steps = 15,
            colors = colors
        )
    }
}

private fun launchTtsSettings(context: Context) {
    Intent().apply {
        action = "com.android.settings.TTS_SETTINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }.also {
        context.startActivity(it)
    }
}
