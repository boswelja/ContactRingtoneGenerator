package com.boswelja.contactringtonegenerator.settings.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ui.common.CheckboxPreference
import com.boswelja.contactringtonegenerator.ui.common.SliderPreference
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel()
    val context = LocalContext.current
    Column {
        ListItem(
            text = { Text(stringResource(R.string.launch_tts_settings_title)) },
            icon = { },
            modifier = Modifier.clickable { launchTtsSettings(context) }
        )
        SliderPreference(
            text = stringResource(R.string.volume_boost_title),
            value = viewModel.volumeBoostValue,
            valueRange = 0f..3f,
            trailing = {
                val boost = it + 1
                Text("%.1fx".format(boost))
            },
            onSliderValueChanged = {
                viewModel.volumeBoostValue = it
            },
            onSliderValueFinished = {
                viewModel.updateVolumeBoost()
            }
        )
        CheckboxPreference(
            text = stringResource(R.string.multithread_title),
            secondaryText = stringResource(R.string.multithread_summary),
            isChecked = viewModel.multithreadedGeneration,
            onCheckChanged = {
                viewModel.multithreadedGeneration = it
                viewModel.updateMultithreadedGeneration()
            }
        )
        ListItem(
            text = { Text(stringResource(R.string.reset_ringtones_title)) },
            secondaryText = { Text(stringResource(R.string.reset_ringtones_summary)) },
            icon = { },
            modifier = Modifier.clickable { viewModel.resetContactRingtones() }
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
