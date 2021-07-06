package com.boswelja.contactringtonegenerator.entry.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.contactringtonegenerator.R
import timber.log.Timber

@Composable
fun GetStartedScreen(
    modifier: Modifier = Modifier,
    onNavigateNext: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        if (grantResults.all { it.value }) {
            onNavigateNext()
        } else {
            Timber.w("Permission denied")
        }
    }
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Image(
            context.packageManager
                .getApplicationIcon(context.packageName).toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.welcome_to),
            style = MaterialTheme.typography.h5
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.h4
        )
        Spacer(Modifier.fillMaxHeight(0.2f))
        ExtendedFloatingActionButton(
            text = {
                Text(stringResource(R.string.get_started))
            },
            icon = {
                Icon(Icons.Default.NavigateNext, stringResource(R.string.next))
            },
            onClick = {
                if (hasContactsPermission(context)) {
                    onNavigateNext()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.WRITE_CONTACTS,
                            Manifest.permission.READ_CONTACTS
                        )
                    )
                }
            }
        )
    }
}

private fun hasContactsPermission(context: Context): Boolean {
    return hasPermission(
        context, Manifest.permission.WRITE_CONTACTS
    ) && hasPermission(context, Manifest.permission.READ_CONTACTS)
}

private fun hasPermission(
    context: Context,
    permission: String
): Boolean {
    return ContextCompat.checkSelfPermission(
        context, permission
    ) == PackageManager.PERMISSION_GRANTED
}
