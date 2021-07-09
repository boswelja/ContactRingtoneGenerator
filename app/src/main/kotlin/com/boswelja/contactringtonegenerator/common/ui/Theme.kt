package com.boswelja.contactringtonegenerator.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green200 = Color(0xffa5d6a7)
private val Green500 = Color(0xff4caf50)

private val DarkColors = darkColors(
    primary = Green200,
    primaryVariant = Green200,
    secondary = Green200,
    secondaryVariant = Green200,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF212121),
    onSurface = Color.White
)
private val LightColors = lightColors(
    primary = Green500,
    primaryVariant = Green500,
    secondary = Green500,
    secondaryVariant = Green500,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.White
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
