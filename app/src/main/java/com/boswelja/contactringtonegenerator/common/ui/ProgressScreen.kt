package com.boswelja.contactringtonegenerator.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProgressScreen(
    progress: Float = 0f,
    status: String,
    step: String
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.h4
        )
        Text(
            text = step,
            style = MaterialTheme.typography.h5
        )
        if (progress > 0) {
            LinearProgressIndicator(
                progress = progress
            )
        } else {
            LinearProgressIndicator()
        }
    }
}
