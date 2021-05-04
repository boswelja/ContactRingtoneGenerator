package com.boswelja.contactringtonegenerator.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Green200 = Color(0xff66bb6a)
private val Green500 = Color(0xff43a047)

private val DarkColors = darkColors(
    primary = Green200,
    primaryVariant = Green200,
    secondary = Green200,
    secondaryVariant = Green200
)
private val LightColors = lightColors(
    primary = Green500,
    primaryVariant = Green500,
    secondary = Green500,
    secondaryVariant = Green500
)

private val shapes = Shapes(
    small = RoundedCornerShape(50),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        shapes = shapes,
        content = content
    )
}
