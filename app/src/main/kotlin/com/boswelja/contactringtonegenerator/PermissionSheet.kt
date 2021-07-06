package com.boswelja.contactringtonegenerator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.StringJoiner

@Composable
fun PermissionSheetContent(
    onGrantClick: () -> Unit = { }
) {
    val permissionString = StringJoiner("\n")
    permissionString.add(stringResource(R.string.permission_missing_read_contacts))
    permissionString.add(stringResource(R.string.permission_missing_write_contacts))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.permission_missing_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Text(
            permissionString.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )
        Button(
            onClick = onGrantClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.grant))
        }
    }
}
