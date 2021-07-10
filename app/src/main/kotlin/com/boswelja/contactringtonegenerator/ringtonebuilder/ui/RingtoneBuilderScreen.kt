package com.boswelja.contactringtonegenerator.ringtonebuilder.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.WizardViewModel
import com.boswelja.contactringtonegenerator.common.ui.ChoiceChip
import com.boswelja.contactringtonegenerator.common.ui.NextButton
import com.boswelja.contactringtonegenerator.ringtonebuilder.AllCategories
import com.boswelja.contactringtonegenerator.ringtonebuilder.Choice
import com.boswelja.contactringtonegenerator.ringtonebuilder.ChoiceCategory
import com.boswelja.contactringtonegenerator.ringtonebuilder.Utils
import com.boswelja.contactringtonegenerator.ringtonegen.item.ContactDataItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomAudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomTextItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun RingtoneBuilderScreen(
    viewModel: WizardViewModel,
    onNavigateNext: () -> Unit
) {
    var nextButtonVisible by remember {
        mutableStateOf(viewModel.isRingtoneValid)
    }

    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            RingtoneStructureList(
                modifier = Modifier.weight(1f),
                contentPaddingValues = PaddingValues(vertical = 8.dp),
                structure = viewModel.ringtoneStructure,
                onActionClicked = { },
                onItemRemoved = {
                    viewModel.ringtoneStructure.remove(it)
                    nextButtonVisible = viewModel.isRingtoneValid
                },
                onDataValidityChanged = {
                    nextButtonVisible = viewModel.isRingtoneValid
                }
            )
            Divider()
            ChoicePicker(Modifier.fillMaxWidth()) {
                viewModel.ringtoneStructure.add(it.createStructureItem())
                nextButtonVisible = viewModel.isRingtoneValid
            }
        }
        NextButton(
            enabled = nextButtonVisible,
            onClick = onNavigateNext
        )
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
    onDataValidityChanged: (Boolean) -> Unit
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

@Composable
fun ChoicePicker(
    modifier: Modifier = Modifier,
    onChoiceClicked: (Choice) -> Unit
) {
    Column(
        modifier = modifier.padding(top = 16.dp, bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AllCategories.forEach {
            ChoiceCategoryItem(category = it, onItemClick = onChoiceClicked)
        }
    }
}

@Composable
fun ChoiceCategoryItem(
    modifier: Modifier = Modifier,
    category: ChoiceCategory,
    onItemClick: (Choice) -> Unit
) {
    val categoryName = stringResource(category.textRes)
    Column(modifier) {
        Row(
            Modifier
                .semantics(mergeDescendants = true) { contentDescription = categoryName }
                .padding(horizontal = 16.dp)
        ) {
            Icon(category.icon, null)
            Spacer(Modifier.width(4.dp))
            Text(categoryName)
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(category.choices) { choice ->
                ChoiceItem(
                    item = choice,
                    onClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun ChoiceItem(
    modifier: Modifier = Modifier,
    item: Choice,
    onClick: (Choice) -> Unit
) {
    ChoiceChip(
        modifier = modifier,
        onClick = { onClick(item) }
    ) {
        Text(stringResource(item.textRes))
    }
}
