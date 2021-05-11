package com.boswelja.contactringtonegenerator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import com.boswelja.contactringtonegenerator.ringtonebuilder.RingtoneBuilderScreen
import com.boswelja.contactringtonegenerator.ringtonegen.Result
import com.boswelja.contactringtonegenerator.settings.ui.SettingsScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    @FlowPreview
    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                var currentDestination by remember { mutableStateOf(Destination.GET_STARTED) }
                var nextVisible by remember { mutableStateOf(true) }
                Scaffold(
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        if (nextVisible) {
                            MainFAB(currentDestination) {
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
                    MainScreen(
                        currentDestination = currentDestination,
                        onDestinationChange = { currentDestination = it },
                        onNextVisibleChange = {
                            if (nextVisible != it) {
                                Timber.d("Setting next button visibility to %s", it)
                                nextVisible = it
                            }
                        }
                    )
                }
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun MainScreen(
        currentDestination: Destination,
        onNextVisibleChange: (Boolean) -> Unit,
        onDestinationChange: (Destination) -> Unit
    ) {
        val viewModel: WizardViewModel = viewModel()
        Crossflow(targetState = currentDestination) { destination ->
            when (destination) {
                Destination.GET_STARTED -> {
                    onNextVisibleChange(true)
                    GetStartedScreen(
                        onSettingsClick = {
                            onDestinationChange(Destination.SETTINGS)
                        }
                    )
                }
                Destination.SETTINGS -> {
                    onNextVisibleChange(false)
                    SettingsScreen()
                }
                Destination.CONTACT_PICKER -> {
                    onNextVisibleChange(viewModel.selectedContacts.isNotEmpty())
                    ContactPickerScreen(
                        viewModel = viewModel,
                        onNextVisibleChange = onNextVisibleChange
                    )
                }
                Destination.RINGTONE_BUILDER -> {
                    onNextVisibleChange(viewModel.isRingtoneValid)
                    RingtoneBuilderScreen(
                        viewModel = viewModel,
                        onNextVisibleChange = onNextVisibleChange
                    )
                }
                Destination.PROGRESS -> {
                    onNextVisibleChange(false)
                    ProgressScreen(
                        status = "",
                        step = "",
                    )
                }
                Destination.RESULT -> {
                    onNextVisibleChange(true)
                    ResultScreen(result = Result.UNKNOWN)
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

    enum class Destination {
        GET_STARTED,
        SETTINGS,
        CONTACT_PICKER,
        RINGTONE_BUILDER,
        PROGRESS,
        RESULT
    }
}
