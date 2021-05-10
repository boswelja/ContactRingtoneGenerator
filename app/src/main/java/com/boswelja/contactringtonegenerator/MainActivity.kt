package com.boswelja.contactringtonegenerator

import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.contactringtonegenerator.common.ui.AppTheme
import com.boswelja.contactringtonegenerator.common.ui.Crossflow
import com.boswelja.contactringtonegenerator.common.ui.ProgressScreen
import com.boswelja.contactringtonegenerator.contactpicker.ui.ContactPickerScreen
import com.boswelja.contactringtonegenerator.entry.ui.GetStartedScreen
import com.boswelja.contactringtonegenerator.result.ui.ResultScreen
import com.boswelja.contactringtonegenerator.ringtonebuilder.PickRingtone
import com.boswelja.contactringtonegenerator.ringtonebuilder.RingtoneBuilderScreen
import com.boswelja.contactringtonegenerator.ringtonebuilder.RingtoneCreatorViewModel
import com.boswelja.contactringtonegenerator.ringtonegen.Result
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import com.boswelja.contactringtonegenerator.settings.ui.SettingsScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val audioPickerLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { }

    private val ringtonePickerLauncher: ActivityResultLauncher<Int> = registerForActivityResult(
        PickRingtone()
    ) { }

    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }

    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun MainScreen() {
        var currentDestination by remember { mutableStateOf(Destination.GET_STARTED) }
        var nextVisible by remember { mutableStateOf(true) }
        Scaffold(
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                AnimatedVisibility(visible = nextVisible) {
                    MainFAB(destination = currentDestination) {
                        when (currentDestination) {
                            Destination.GET_STARTED ->
                                currentDestination = Destination.CONTACT_PICKER
                            Destination.CONTACT_PICKER ->
                                currentDestination = Destination.RINGTONE_BUILDER
                            Destination.RINGTONE_BUILDER ->
                                currentDestination = Destination.PROGRESS
                            Destination.RESULT -> finish()
                            else ->
                                Timber.w("Tried navigating from unsupported Destination")
                        }
                    }
                }
            }
        ) {
            Crossflow(targetState = currentDestination) {
                when (it) {
                    Destination.GET_STARTED -> {
                        GetStartedScreen(
                            onSettingsClick = {
                                currentDestination = Destination.SETTINGS
                            }
                        )
                    }
                    Destination.SETTINGS -> {
                        SettingsScreen {
                            launchTtsSettings()
                        }
                    }
                    Destination.CONTACT_PICKER -> {
                        ContactPickerScreen { readyToContinue ->
                            nextVisible = readyToContinue
                        }
                    }
                    Destination.RINGTONE_BUILDER -> {
                        val ringtoneCreatorModel: RingtoneCreatorViewModel = viewModel()
                        val structure = ringtoneCreatorModel.ringtoneStructure
                        RingtoneBuilderScreen(
                            structure = structure,
                            onItemAdded = { item ->
                                ringtoneCreatorModel.ringtoneStructure.add(item)
                            },
                            onItemRemoved = { item ->
                                ringtoneCreatorModel.ringtoneStructure.remove(item)
                            },
                            onActionClicked = { item ->
                                when (item.dataType) {
                                    StructureItem.DataType.AUDIO_FILE ->
                                        audioPickerLauncher.launch("audio/*")
                                    StructureItem.DataType.SYSTEM_RINGTONE ->
                                        ringtonePickerLauncher.launch(RingtoneManager.TYPE_RINGTONE)
                                    else -> Timber.w("Unknown action clicked")
                                }
                            }
                        )
                    }
                    Destination.PROGRESS -> {
                        ProgressScreen(
                            status = "",
                            step = "",
                        )
                    }
                    Destination.RESULT -> {
                        ResultScreen(result = Result.UNKNOWN)
                    }
                }
            }
        }
    }

    @Composable
    fun MainFAB(
        destination: Destination,
        onClick: () -> Unit
    ) {
        val (text, icon) = when (destination) {
            Destination.GET_STARTED ->
                Pair(stringResource(R.string.get_started), Icons.Default.NavigateNext)
            Destination.CONTACT_PICKER,
            Destination.RINGTONE_BUILDER ->
                Pair(stringResource(R.string.next), Icons.Default.NavigateNext)
            Destination.PROGRESS ->
                Pair("", Icons.Default.NavigateNext)
            Destination.RESULT ->
                Pair(stringResource(R.string.finish), Icons.Default.Check)
            Destination.SETTINGS -> Pair("", Icons.Default.NavigateNext)
        }
        ExtendedFloatingActionButton(
            text = { Text(text) },
            icon = { Icon(icon, null) },
            onClick = onClick
        )
    }

    private fun launchTtsSettings() {
        Intent().apply {
            action = "com.android.settings.TTS_SETTINGS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            startActivity(it)
        }
    }
    enum class Destination {
        GET_STARTED,
        SETTINGS,
        CONTACT_PICKER,
        RINGTONE_BUILDER,
        PROGRESS,
        RESULT
    }
}
