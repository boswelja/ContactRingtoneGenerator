package com.boswelja.contactringtonegenerator.ringtonebuilder

import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.WizardViewModel
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureChoice
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.Utils
import timber.log.Timber

@ExperimentalMaterialApi
@Composable
fun RingtoneBuilderScreen(
    viewModel: WizardViewModel,
    onNextVisibleChange: (Boolean) -> Unit
) {
    var editingItemIndex: Int = remember { -1 }
    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if (it == null || editingItemIndex < 0) return@rememberLauncherForActivityResult
        Timber.d("Setting %s data to %s", editingItemIndex, it)
        val item = viewModel.ringtoneStructure.removeAt(editingItemIndex)
        item.setData(it)
        viewModel.ringtoneStructure.add(editingItemIndex, item)
        editingItemIndex = -1
        onNextVisibleChange(viewModel.isRingtoneValid)
    }
    val ringtonePickerLauncher = rememberLauncherForActivityResult(PickRingtone()) {
        if (it == null || editingItemIndex < 0) return@rememberLauncherForActivityResult
        Timber.d("Setting %s data to %s", editingItemIndex, it)
        val item = viewModel.ringtoneStructure.removeAt(editingItemIndex)
        item.setData(it)
        viewModel.ringtoneStructure.add(editingItemIndex, item)
        editingItemIndex = -1
        onNextVisibleChange(viewModel.isRingtoneValid)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        StructureChoices { choice ->
            Timber.d("Adding %s", choice)
            val item = choice.createStructureItem()
            viewModel.ringtoneStructure.add(item)
            onNextVisibleChange(viewModel.isRingtoneValid)
        }
        Divider()
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(viewModel.ringtoneStructure) { index, item ->
                val dismissState = rememberDismissState {
                    if (it != DismissValue.Default) {
                        viewModel.ringtoneStructure.removeAt(index)
                        onNextVisibleChange(viewModel.isRingtoneValid)
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
                            item = item,
                            onDataValidityChanged = {
                                if (!it) onNextVisibleChange(false)
                                else onNextVisibleChange(viewModel.isRingtoneValid)
                            },
                            onActionClicked = {
                                editingItemIndex = index
                                when (it.dataType) {
                                    StructureItem.DataType.AUDIO_FILE ->
                                        audioPickerLauncher.launch("audio/*")
                                    StructureItem.DataType.SYSTEM_RINGTONE ->
                                        ringtonePickerLauncher
                                            .launch(RingtoneManager.TYPE_RINGTONE)
                                    else -> Timber.w("Unknown action clicked")
                                }
                            }
                        )
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
    onDataValidityChanged: (Boolean) -> Unit,
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
                            onDataValidityChanged(item.isDataValid)
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
