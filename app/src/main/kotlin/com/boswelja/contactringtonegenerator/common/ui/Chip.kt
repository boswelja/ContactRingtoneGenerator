package com.boswelja.contactringtonegenerator.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ChoiceChip(
    modifier: Modifier = Modifier,
    height: Dp = 32.dp,
    shape: Shape = MaterialTheme.shapes.small,
    backgroundColor: Color = MaterialTheme.colors.surface,
    borderStroke: BorderStroke =
        BorderStroke(1.dp, LocalContentColor.current.copy(alpha = 0.12f)),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        border = borderStroke,
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp)
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
