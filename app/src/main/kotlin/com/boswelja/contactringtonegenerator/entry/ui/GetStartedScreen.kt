package com.boswelja.contactringtonegenerator.entry.ui

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
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.compose.rememberNavController
import com.boswelja.contactringtonegenerator.Destination
import com.boswelja.contactringtonegenerator.R

@Composable
fun GetStartedScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
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
                navController.navigate(Destination.CONTACT_PICKER.name)
            }
        )
    }
}
