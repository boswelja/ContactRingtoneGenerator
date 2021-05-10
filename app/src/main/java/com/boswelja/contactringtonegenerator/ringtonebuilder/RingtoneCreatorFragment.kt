package com.boswelja.contactringtonegenerator.ringtonebuilder

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.WizardViewModel
import com.boswelja.contactringtonegenerator.common.ui.AppTheme
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureChoice
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.Utils
import timber.log.Timber

class RingtoneCreatorFragment : Fragment() {

    private val wizardModel: WizardViewModel by activityViewModels()

    private val audioPickerLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { }

    private val ringtonePickerLauncher: ActivityResultLauncher<Int> = registerForActivityResult(
        PickRingtone()
    ) { }

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel: RingtoneCreatorViewModel = viewModel()
                val structure = viewModel.ringtoneStructure
                AppTheme {
                    Scaffold(
                        floatingActionButtonPosition = FabPosition.End,
                        floatingActionButton = {
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.next)) },
                                icon = { Icon(Icons.Outlined.NavigateNext, null) },
                                onClick = {
                                    wizardModel.submitRingtoneStructure(structure)
                                }
                            )
                        }
                    ) {
                        RingtoneBuilderScreen(
                            structure = structure,
                            onItemAdded = { item -> viewModel.ringtoneStructure.add(item) },
                            onActionClicked = { item ->
                                when (item.dataType) {
                                    StructureItem.DataType.AUDIO_FILE ->
                                        audioPickerLauncher.launch("audio/*")
                                    StructureItem.DataType.SYSTEM_RINGTONE ->
                                        ringtonePickerLauncher.launch(RingtoneManager.TYPE_RINGTONE)
                                    else -> Timber.w("Unknown action clicked")
                                }
                            },
                            onItemRemoved = { item -> viewModel.ringtoneStructure.remove(item) },
                            modifier = Modifier.padding(it)
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun RingtoneBuilderScreen(
    structure: List<StructureItem<*>>?,
    onItemAdded: (StructureItem<*>) -> Unit,
    onActionClicked: (StructureItem<*>) -> Unit,
    onItemRemoved: (StructureItem<*>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        StructureChoices { choice ->
            Timber.d("Adding %s", choice)
            onItemAdded(choice.createStructureItem())
        }
        Divider()
        LazyColumn(Modifier.weight(1f)) {
            structure?.let { structure ->
                items(structure) { item ->
                    val dismissState = rememberDismissState {
                        if (it != DismissValue.Default) {
                            onItemRemoved(item)
                            true
                        } else false
                    }
                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            val direction =
                                dismissState.dismissDirection ?: return@SwipeToDismiss
                            val color = Color.LightGray
                            val alignment = when (direction) {
                                DismissDirection.StartToEnd -> Alignment.CenterStart
                                DismissDirection.EndToStart -> Alignment.CenterEnd
                            }
                            val icon = Icons.Outlined.Delete

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 32.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    ) {
                        Card(
                            elevation = animateDpAsState(
                                if (dismissState.dismissDirection != null) 4.dp else 0.dp
                            ).value
                        ) {
                            StructureItem(
                                item,
                                onActionClicked
                            )
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun StructureItem(
    item: StructureItem<*>,
    onActionClicked: (StructureItem<*>) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        icon = { Icon(item.icon, null) },
        text = {
            when (item.dataType) {
                StructureItem.DataType.IMMUTABLE ->
                    Text(stringResource(item.labelRes))
                StructureItem.DataType.CUSTOM_TEXT -> {
                    var currentText by mutableStateOf(item.data?.toString() ?: "")
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = {
                            currentText = it
                            item.setData(it)
                        },
                        singleLine = true
                    )
                }
                StructureItem.DataType.AUDIO_FILE,
                StructureItem.DataType.SYSTEM_RINGTONE -> {
                    val context = LocalContext.current
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Utils.getDisplayText(context, item.data as Uri?)
                                ?: stringResource(item.labelRes),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onActionClicked(item) }) {
                            Text(stringResource(R.string.browse))
                        }
                    }
                }
            }
        },
        // TODO re-enable drag to reorder
//            trailing = {
//                Icon(Icons.Filled.DragHandle, null)
//            },
        modifier = modifier
    )
}

@Composable
fun StructureChoices(
    onItemClick: (StructureChoice) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(StructureChoice.ALL) { choice ->
            ItemChip(choice, onItemClick)
        }
    }
}

@Composable
fun ItemChip(
    item: StructureChoice,
    onClick: (StructureChoice) -> Unit
) {
    Surface(
        color = Color.LightGray,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            Modifier
                .clickable { onClick(item) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(item.icon, null)
            Text(stringResource(item.textRes))
        }
    }
}
