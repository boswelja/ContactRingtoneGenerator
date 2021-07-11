package com.boswelja.contactringtonegenerator.ringtonebuilder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.common.ui.ChoiceChip
import com.boswelja.contactringtonegenerator.ringtonebuilder.AllCategories
import com.boswelja.contactringtonegenerator.ringtonebuilder.Choice
import com.boswelja.contactringtonegenerator.ringtonebuilder.ChoiceCategory

@Composable
fun StructureChoicePicker(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismissPicker: () -> Unit,
    onChoiceSelected: (Choice) -> Unit
) {
    if (visible) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismissPicker,
            confirmButton = {
                TextButton(onClick = onDismissPicker) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(stringResource(R.string.ringtone_builder_title))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AllCategories.forEach {
                        ChoiceCategoryItem(category = it) { choice ->
                            onChoiceSelected(choice)
                            onDismissPicker()
                        }
                    }
                }
            }
        )
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
                ChoiceChip(
                    modifier = modifier,
                    onClick = { onItemClick(choice) }
                ) {
                    Text(stringResource(choice.textRes))
                }
            }
        }
    }
}
