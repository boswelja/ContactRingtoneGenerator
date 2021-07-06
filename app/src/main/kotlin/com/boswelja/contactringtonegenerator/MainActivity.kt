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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.boswelja.contactringtonegenerator.common.ui.AppTheme
import com.boswelja.contactringtonegenerator.common.ui.ProgressScreen
import com.boswelja.contactringtonegenerator.contactpicker.ui.ContactPickerScreen
import com.boswelja.contactringtonegenerator.entry.ui.GetStartedScreen
import com.boswelja.contactringtonegenerator.result.ui.ResultScreen
import com.boswelja.contactringtonegenerator.ringtonebuilder.RingtoneBuilderScreen
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
    val navController = rememberNavController()
    val viewModel: WizardViewModel = viewModel()

    NavHost(navController = navController, startDestination = Destination.GET_STARTED.name) {
        composable(Destination.GET_STARTED.name) {
            GetStartedScreen { }
        }
        composable(Destination.CONTACT_PICKER.name) {
            ContactPickerScreen(viewModel = viewModel) { }
        }
        composable(Destination.RINGTONE_BUILDER.name) {
            RingtoneBuilderScreen(viewModel = viewModel) { }
        }
        composable(Destination.PROGRESS.name) {
            ProgressScreen(status = "", step = "")
        }
        composable(Destination.RESULT.name) {
            ResultScreen(result = null)
        }
    }
}
