package com.boswelja.contactringtonegenerator.progress.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.WorkInfo
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.ringtonegen.GeneratorResult
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGeneratorWorker

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    workInfo: WorkInfo?,
    onFinished: (GeneratorResult) -> Unit
) {
    // Get most recent progress data
    val progressData = workInfo?.progress ?: Data.EMPTY
    val progress = progressData.getFloat(RingtoneGeneratorWorker.Outputs.Progress, 0f)
    val failCount = progressData.getStringArray(
        RingtoneGeneratorWorker.Outputs.FailedContactLookupKeys
    )?.size ?: 0

    // TODO This probably shouldn't be checked here
    if (workInfo != null && workInfo.outputData != Data.EMPTY) {
        val result = GeneratorResult.valueOf(
            workInfo.outputData.getString(RingtoneGeneratorWorker.Outputs.Result)!!
        )
        onFinished(result)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (progress > 0) {
            LinearProgressIndicator(
                progress = progress
            )
        } else {
            LinearProgressIndicator()
        }
        Spacer(Modifier.height(8.dp))
        if (failCount > 0) {
            val failCountText = LocalContext.current.resources.getQuantityString(
                R.plurals.progress_fail_count, failCount, failCount
            )
            Text(
                text = failCountText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle1
            )
        } else {
            Text(
                text = stringResource(R.string.progress_status_ok),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle1
            )
        }
    }
}
