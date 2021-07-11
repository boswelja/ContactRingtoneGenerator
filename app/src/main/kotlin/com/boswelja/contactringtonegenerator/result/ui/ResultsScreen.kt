package com.boswelja.contactringtonegenerator.result.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.GeneratorResult

@Composable
fun ResultScreen(
    generatorResult: GeneratorResult?,
    modifier: Modifier = Modifier
) {
    // Get result display info
    val (icon, resultTitle, resultText) = when (generatorResult) {
        GeneratorResult.FAILED ->
            Triple(
                Icons.Outlined.ErrorOutline,
                R.string.result_failed_title,
                R.string.result_failed_status
            )
        GeneratorResult.MIXED ->
            Triple(
                Icons.Outlined.Warning,
                R.string.result_mixed_title,
                R.string.result_mixed_status
            )
        GeneratorResult.SUCCESSFUL ->
            Triple(
                Icons.Outlined.CheckCircleOutline,
                R.string.result_success_title,
                R.string.result_success_status
            )
        else ->
            Triple(
                Icons.Outlined.HelpOutline,
                R.string.result_unknown_title,
                R.string.result_unknown_status
            )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(180.dp)
        )
        Text(
            text = stringResource(resultTitle),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(resultText),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center
        )
    }
}
