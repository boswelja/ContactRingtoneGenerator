package com.boswelja.contactringtonegenerator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    @FlowPreview
    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
            val coroutineScope = rememberCoroutineScope()
            AppTheme {
                BackdropScaffold(
                    scaffoldState = scaffoldState,
                    appBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.app_name))
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (scaffoldState.isRevealed) {
                                                scaffoldState.conceal()
                                            } else {
                                                scaffoldState.reveal()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.settings_title)
                                    )
                                }
                            },
                            backgroundColor = Color.Transparent,
                            elevation = 0.dp
                        )
                    },
                    backLayerContent = {
                        SettingsScreen()
                    },
                    frontLayerContent = {
                        MainScreen()
                    }
                )
            }
        }
    }
}

enum class Destination {
    GET_STARTED,
    CONTACT_PICKER,
    RINGTONE_BUILDER,
    PROGRESS,
    RESULT
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun MainScreen() {
    var currentDestination by remember { mutableStateOf(Destination.GET_STARTED) }
    val viewModel: WizardViewModel = viewModel()
    Crossflow(targetState = currentDestination) { destination ->
        when (destination) {
            Destination.GET_STARTED -> {
                GetStartedScreen { }
            }
            Destination.CONTACT_PICKER -> {
                ContactPickerScreen(
                    viewModel = viewModel,
                    onNextVisibleChange = { }
                )
            }
            Destination.RINGTONE_BUILDER -> {
                RingtoneBuilderScreen(
                    viewModel = viewModel,
                    onNextVisibleChange = { }
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
