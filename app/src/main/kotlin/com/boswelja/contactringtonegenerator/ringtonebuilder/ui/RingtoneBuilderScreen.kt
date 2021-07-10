package com.boswelja.contactringtonegenerator.ringtonebuilder.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.boswelja.contactringtonegenerator.common.ui.NextButton
import com.boswelja.contactringtonegenerator.ringtonebuilder.Utils
import com.boswelja.contactringtonegenerator.ringtonegen.item.ContactDataItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomAudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomTextItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun RingtoneBuilderScreen(
    modifier: Modifier = Modifier,
    viewModel: WizardViewModel,
    onNavigateNext: () -> Unit
) {
    var nextButtonVisible by remember {
        mutableStateOf(viewModel.isRingtoneValid)
    }
    var choicePickerVisible by remember {
        mutableStateOf(false)
    }

    Box(modifier) {
        RingtoneStructureList(
            modifier = Modifier.fillMaxSize(),
            contentPaddingValues = PaddingValues(top = 8.dp, bottom = 72.dp),
            structure = viewModel.ringtoneStructure,
            onActionClicked = { },
            onItemRemoved = {
                viewModel.ringtoneStructure.remove(it)
                nextButtonVisible = viewModel.isRingtoneValid
            },
            onDataValidityChanged = {
                nextButtonVisible = viewModel.isRingtoneValid
            },
            onShowPicker = {
                choicePickerVisible = true
            }
        )
        NextButton(
            enabled = nextButtonVisible,
            onClick = onNavigateNext
        )
    }

    StructureChoicePicker(
        visible = choicePickerVisible,
        onDismissPicker = { choicePickerVisible = false }
    ) {
        viewModel.ringtoneStructure.add(it.createStructureItem())
        nextButtonVisible = viewModel.isRingtoneValid
    }
}

@ExperimentalMaterialApi
@Composable
fun RingtoneStructureList(
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(),
    structure: List<StructureItem>,
    onActionClicked: (StructureItem) -> Unit,
    onItemRemoved: (StructureItem) -> Unit,
    onDataValidityChanged: (Boolean) -> Unit,
    onShowPicker: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPaddingValues
    ) {
        items(
            items = structure,
            key = { item -> item.id }
        ) { item ->
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

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 32.dp),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            ) {
                val elevation by animateDpAsState(
                    if (dismissState.dismissDirection != null) 4.dp else 0.dp
                )
                Card(elevation = elevation) {
                    StructureItem(
                        item = item,
                        onDataValidityChanged = onDataValidityChanged,
                        onActionClicked = onActionClicked
                    )
                }
            }
        }
        item {
            if (structure.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Divider()
            }
            ListItem(
                modifier = Modifier.clickable(onClick = onShowPicker),
                icon = {
                    Icon(Icons.Default.Add, null)
                },
                text = {
                    Text(stringResource(R.string.add))
                }
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun StructureItem(
    item: StructureItem,
    onActionClicked: (StructureItem) -> Unit,
    onDataValidityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        text = {
            when (item) {
                is ContactDataItem ->
                    Text(stringResource(item.textRes))
                is CustomTextItem -> {
                    OutlinedTextField(
                        value = item.data!!,
                        onValueChange = {
                            item.data = it
                            // item.setData(it)
                            onDataValidityChanged(item.isDataValid)
                        },
                        singleLine = true
                    )
                }
                is CustomAudioItem -> {
                    val context = LocalContext.current
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Utils.getDisplayText(context, item.audioUri)
                                ?: "",
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
