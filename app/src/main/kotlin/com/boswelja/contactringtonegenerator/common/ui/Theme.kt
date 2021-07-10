package com.boswelja.contactringtonegenerator.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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
    background = Color(0xFFF5F5F5),
    onBackground = Color.Black,
    surface = Color.White
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors
    ) {
        val statusBarColor = MaterialTheme.colors.background
        val navBarColor = MaterialTheme.colors.surface
        LaunchedEffect(darkTheme) {
            systemUiController.setStatusBarColor(statusBarColor)
            systemUiController.setNavigationBarColor(navBarColor)
        }
        content()
    }
}
