package com.boswelja.contactringtonegenerator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExperimentalExpeditedWork
import com.boswelja.contactringtonegenerator.common.ui.AppTheme
import com.boswelja.contactringtonegenerator.confirmation.ui.ConfirmationScreen
import com.boswelja.contactringtonegenerator.contactpicker.ui.ContactPickerScreen
import com.boswelja.contactringtonegenerator.entry.ui.GetStartedScreen
import com.boswelja.contactringtonegenerator.progress.ui.ProgressScreen
import com.boswelja.contactringtonegenerator.result.ui.ResultScreen
import com.boswelja.contactringtonegenerator.ringtonebuilder.ui.RingtoneBuilderScreen
import com.boswelja.contactringtonegenerator.settings.ui.SettingsScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @ExperimentalExpeditedWork
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
            val coroutineScope = rememberCoroutineScope()
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()

            AppTheme {
                BackdropScaffold(
                    scaffoldState = scaffoldState,
                    appBar = {
                        TopAppBar(
                            title = destinationTitle(currentBackStackEntry?.destination?.route),
                            onShowSettings = {
                                coroutineScope.launch {
                                    if (scaffoldState.isConcealed) {
                                        scaffoldState.reveal()
                                    } else {
                                        scaffoldState.conceal()
                                    }
                                }
                            }
                        )
                    },
                    backLayerBackgroundColor = MaterialTheme.colors.background,
                    backLayerContent = {
                        BackdropContent(
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    frontLayerContent = {
                        MainScreen(
                            navController = navController,
                            onFinished = { finish() }
                        )
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
    CONFIRMATION,
    PROGRESS,
    RESULT
}

@Composable
fun destinationTitle(destinationRoute: String?): String {
    return when (destinationRoute) {
        Destination.CONTACT_PICKER.name ->
            stringResource(R.string.contact_picker_title)
        Destination.RINGTONE_BUILDER.name -> stringResource(R.string.ringtone_builder_title)
        Destination.CONFIRMATION.name -> stringResource(R.string.confirmation_title)
        Destination.PROGRESS.name -> stringResource(R.string.working_title)
        Destination.RESULT.name -> stringResource(R.string.result_title)
        else -> stringResource(R.string.app_name)
    }
}

@ExperimentalAnimationApi
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    onShowSettings: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Crossfade(targetState = title) {
                Text(it)
            }
        },
        actions = {
            IconButton(onShowSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_title)
                )
            }
        },
        backgroundColor = Color.Transparent,
        contentColor = LocalContentColor.current,
        elevation = 0.dp
    )
}

@ExperimentalExpeditedWork
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onFinished: () -> Unit
) {
    val viewModel: WizardViewModel = viewModel()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Destination.GET_STARTED.name
    ) {
        composable(Destination.GET_STARTED.name) {
            GetStartedScreen(
                Modifier.fillMaxSize()
            ) {
                navController.navigate(Destination.CONTACT_PICKER.name)
            }
        }
        composable(Destination.CONTACT_PICKER.name) {
            ContactPickerScreen(
                modifier = Modifier.fillMaxSize(),
                selectedContacts = viewModel.selectedContacts,
                onContactSelectionChanged = { contact, isSelected ->
                    if (isSelected) {
                        viewModel.selectContact(contact)
                    } else {
                        viewModel.deselectContact(contact)
                    }
                }
            ) {
                navController.navigate(Destination.RINGTONE_BUILDER.name)
            }
        }
        composable(Destination.RINGTONE_BUILDER.name) {
            RingtoneBuilderScreen(viewModel = viewModel) {
                navController.navigate(Destination.CONFIRMATION.name)
            }
        }
        composable(Destination.CONFIRMATION.name) {
            ConfirmationScreen(
                modifier = Modifier.fillMaxSize(),
                contentPaddingValues = PaddingValues(16.dp),
                selectedContactCount = viewModel.selectedContacts.count(),
                onStartClicked = {
                    viewModel.startWorker()
                    navController.navigate(Destination.PROGRESS.name) {
                        popUpTo(Destination.GET_STARTED.name) {
                            inclusive = true
                        }
                    }
                },
                onReturnToContactSelected = {
                    navController.popBackStack(Destination.CONTACT_PICKER.name, false)
                },
                onReturnToRingtoneSelected = {
                    navController.popBackStack(Destination.RINGTONE_BUILDER.name, false)
                }
            )
        }
        composable(Destination.PROGRESS.name) {
            val workInfo by viewModel.getWorkInfo().observeAsState()
            ProgressScreen(
                modifier = Modifier.fillMaxSize(),
                workInfo = workInfo,
                onFinished = { result ->
                    viewModel.generatorResult = result
                    navController.navigate(Destination.RESULT.name) {
                        popUpTo(Destination.PROGRESS.name) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Destination.RESULT.name) {
            viewModel.generatorResult?.let { result ->
                ResultScreen(
                    result = result,
                    onDoneClicked = onFinished
                )
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun BackdropContent(
    modifier: Modifier = Modifier
) {
    SettingsScreen(modifier)
}
