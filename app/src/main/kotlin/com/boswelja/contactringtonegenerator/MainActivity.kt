package com.boswelja.contactringtonegenerator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.boswelja.contactringtonegenerator.common.LocalSearchComposition
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
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()

            var searchQuery by remember {
                mutableStateOf("")
            }
            val isSearchVisible = currentBackStackEntry?.destination?.route ==
                Destination.CONTACT_PICKER.name

            var isSettingsVisible by remember {
                mutableStateOf(false)
            }

            AppTheme {
                BackdropScaffold(
                    gesturesEnabled = isSearchVisible || isSettingsVisible,
                    scaffoldState = scaffoldState,
                    appBar = {
                        TopAppBar(
                            title = when (currentBackStackEntry?.destination?.route) {
                                else -> stringResource(R.string.app_name)
                            },
                            isSearchVisible = isSearchVisible,
                            onShowSearch = {
                                coroutineScope.launch {
                                    if (scaffoldState.isRevealed) {
                                        scaffoldState.conceal()
                                    } else {
                                        scaffoldState.reveal()
                                    }
                                }
                            },
                            onShowSettings = {
                                coroutineScope.launch {
                                    isSettingsVisible = !isSettingsVisible
                                    if (isSettingsVisible) {
                                        scaffoldState.reveal()
                                    } else if (!isSearchVisible) {
                                        scaffoldState.conceal()
                                    }
                                }
                            }
                        )
                    },
                    backLayerContent = {
                        BackdropContent(
                            searchQuery = searchQuery,
                            onSearchQueryChanged = { searchQuery = it },
                            isSettingsVisible = isSettingsVisible,
                            isSearchVisible = isSearchVisible
                        )
                    },
                    frontLayerContent = {
                        CompositionLocalProvider(
                            LocalSearchComposition provides searchQuery
                        ) {
                            MainScreen(
                                navController = navController
                            )
                        }
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
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    isSearchVisible: Boolean,
    onShowSearch: () -> Unit,
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
            AnimatedVisibility(isSearchVisible) {
                IconButton(onShowSearch) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_hint)
                    )
                }
            }
            IconButton(onShowSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_title)
                )
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@FlowPreview
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
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
                viewModel = viewModel
            ) {
                navController.navigate(Destination.RINGTONE_BUILDER.name)
            }
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

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun BackdropContent(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    isSettingsVisible: Boolean,
    isSearchVisible: Boolean
) {
    Column(modifier) {
        AnimatedVisibility(isSearchVisible) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text(stringResource(R.string.search_hint))
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        stringResource(R.string.search_hint)
                    )
                },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }
        AnimatedVisibility(isSettingsVisible) {
            SettingsScreen()
        }
    }
}
