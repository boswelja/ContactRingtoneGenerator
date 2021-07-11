package com.boswelja.contactringtonegenerator.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt

@Composable
fun SliderSetting(
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    stepSize: Float = 0.1f,
    value: Float,
    text: @Composable () -> Unit,
    valueText: @Composable () -> Unit,
    onValueChanged: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit
) {
    val steps = remember {
        ((valueRange.endInclusive - valueRange.start) / stepSize).roundToInt()
    }

    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.subtitle1
            ) {
                Box(Modifier.weight(1f)) {
                    text()
                }
            }
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                valueText()
            }
        }
        Slider(
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChanged,
            onValueChangeFinished = { onValueChangeFinished(value) },
            steps = steps
        )
    }
}
