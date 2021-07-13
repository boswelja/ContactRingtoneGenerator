package com.boswelja.contactringtonegenerator.confirmation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.contactringtonegenerator.R

@Composable
fun ConfirmationScreen(
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(),
    selectedContactCount: Int,
    onStartClicked: () -> Unit,
    onReturnToContactSelected: () -> Unit,
    onReturnToRingtoneSelected: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ContactSelectionSummary(
            modifier = Modifier.padding(contentPaddingValues),
            selectedContactCount = selectedContactCount,
            onReturnToContactSelected = onReturnToContactSelected
        )
        Divider()
        RingtoneStructureSummary(
            modifier = Modifier.padding(contentPaddingValues),
            onReturnToRingtoneSelected = onReturnToRingtoneSelected
        )
        Spacer(Modifier.height(64.dp))
        ExtendedFloatingActionButton(
            text = {
                Text(stringResource(R.string.confirmation_start))
            },
            icon = {
                Icon(
                    Icons.Default.NavigateNext,
                    null
                )
            },
            shape = MaterialTheme.shapes.small,
            onClick = onStartClicked
        )
    }
}

@Composable
fun ContactSelectionSummary(
    modifier: Modifier = Modifier,
    selectedContactCount: Int,
    onReturnToContactSelected: () -> Unit
) {
    val selectedContactsString = LocalContext.current.resources.getQuantityString(
        R.plurals.confirmation_selected_contacts, selectedContactCount, selectedContactCount
    )
    SettingSummary(
        modifier = modifier,
        icon = Icons.Default.Contacts,
        text = selectedContactsString,
        buttonText = stringResource(R.string.confirmation_contact_picker_return),
        onButtonClick = onReturnToContactSelected
    )
}

@Composable
fun RingtoneStructureSummary(
    modifier: Modifier = Modifier,
    onReturnToRingtoneSelected: () -> Unit
) {
    SettingSummary(
        modifier = modifier,
        icon = Icons.Default.RingVolume,
        text = stringResource(R.string.confirmation_generator_info),
        buttonText = stringResource(R.string.confirmation_ringtone_builder_return),
        onButtonClick = onReturnToRingtoneSelected
    )
}
