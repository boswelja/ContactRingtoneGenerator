package com.boswelja.contactringtonegenerator.entry.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.common.ui.AppTheme

@Composable
fun GetStartedScreen(
    onSettingsClick: () -> Unit
) {
    AppTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = 64.dp, bottom = 64.dp)
        ) {
            AppInfo()
            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Outlined.Settings, null)
                Text(stringResource(R.string.settings_title))
            }
        }
    }
}

@Composable
fun AppInfo() {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            context.packageManager
                .getApplicationIcon(context.packageName).toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
        Text(
            text = stringResource(R.string.welcome_to),
            style = MaterialTheme.typography.h5
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.h4
        )
    }
}
