package com.boswelja.contactringtonegenerator.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@ExperimentalMaterialApi
@Composable
fun SwitchPreference(
    text: String,
    secondaryText: String? = null,
    icon: ImageVector? = null,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    ListItem(
        text = { Text(text) },
        secondaryText = if (secondaryText != null) { { Text(secondaryText) } } else null,
        icon = { if (icon != null) { Icon(icon, null) } },
        trailing = {
            Switch(checked = isChecked, onCheckedChange = null)
        },
        modifier = Modifier.clickable {
            onCheckChanged(!isChecked)
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun CheckboxPreference(
    text: String,
    secondaryText: String? = null,
    icon: ImageVector? = null,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    ListItem(
        text = { Text(text) },
        secondaryText = if (secondaryText != null) { { Text(secondaryText) } } else null,
        icon = { if (icon != null) { Icon(icon, null) } },
        trailing = {
            Checkbox(checked = isChecked, onCheckedChange = null)
        },
        modifier = Modifier.clickable {
            onCheckChanged(!isChecked)
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun SliderPreference(
    text: String,
    icon: ImageVector? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    value: Float,
    trailing: @Composable (Float) -> Unit = { },
    onSliderValueChanged: (Float) -> Unit,
    onSliderValueFinished: () -> Unit
) {
    ListItem(
        text = { Text(text) },
        icon = { if (icon != null) { Icon(icon, null) } },
        secondaryText = {
            Slider(
                value = value,
                valueRange = valueRange,
                onValueChange = onSliderValueChanged,
                onValueChangeFinished = onSliderValueFinished
            )
        },
        trailing = {
            trailing(value)
        }
    )
}

@Composable
fun HeaderItem(text: String) {
    Box(Modifier.background(MaterialTheme.colors.background)) {
        Text(
            text,
            Modifier
                .fillMaxWidth()
                .padding(start = 72.dp, top = 16.dp, bottom = 8.dp, end = 8.dp),
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
    }
}
