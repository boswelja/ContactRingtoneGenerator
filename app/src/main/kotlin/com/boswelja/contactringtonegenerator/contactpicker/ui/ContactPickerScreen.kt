package com.boswelja.contactringtonegenerator.contactpicker.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.WizardViewModel
import com.boswelja.contactringtonegenerator.common.LocalSearchComposition
import com.boswelja.contactringtonegenerator.contactpicker.Contact
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import timber.log.Timber

@FlowPreview
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun ContactPickerScreen(
    modifier: Modifier = Modifier,
    viewModel: WizardViewModel,
    onNavigateNext: () -> Unit
) {
    val allContacts by viewModel.allContacts.collectAsState(
        emptyList(),
        Dispatchers.IO
    )

    val currentQuery = LocalSearchComposition.current

    val visibleContacts by remember(allContacts, currentQuery) {
        derivedStateOf {
            allContacts.filter { it.displayName.contains(currentQuery, ignoreCase = true) }
        }
    }

    Box(modifier) {
        ContactsList(
            contentPaddingValues = PaddingValues(bottom = 72.dp),
            contacts = visibleContacts,
            selectedContacts = viewModel.selectedContacts,
            onContactSelectionChanged = { contact, isSelected ->
                Timber.d(
                    "Selection changed for %s, is now %s",
                    contact,
                    isSelected
                )
                if (isSelected) {
                    viewModel.selectContacts(listOf(contact.lookupKey))
                } else {
                    viewModel.deselectContacts(listOf(contact.lookupKey))
                }
            }
        )
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            visible = viewModel.selectedContacts.isNotEmpty()
        ) {
            ExtendedFloatingActionButton(
                text = {
                    Text(stringResource(R.string.next))
                },
                icon = {
                    Icon(
                        Icons.Default.NavigateNext,
                        contentDescription = stringResource(R.string.next)
                    )
                },
                onClick = onNavigateNext
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun ContactsList(
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(),
    contacts: List<Contact>,
    selectedContacts: Collection<String>,
    onContactSelectionChanged: (Contact, Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPaddingValues
    ) {
        items(
            items = contacts,
            key = { it.lookupKey }
        ) { contact ->
            val iconModifier = Modifier.size(48.dp)
            val iconTint = LocalContentColor.current.copy(alpha = ContentAlpha.medium)

            val icon by loadContactPhoto(contact)
            var selected by remember {
                mutableStateOf(selectedContacts.contains(contact.lookupKey))
            }

            ListItem(
                text = { Text(contact.displayName) },
                icon = {
                    if (icon != null) {
                        Image(
                            bitmap = icon!!,
                            contentDescription = null,
                            modifier = iconModifier.clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            modifier = iconModifier,
                            tint = iconTint
                        )
                    }
                },
                trailing = {
                    Checkbox(checked = selected, onCheckedChange = null)
                },
                modifier = Modifier.clickable {
                    selected = !selected
                    onContactSelectionChanged(contact, selected)
                }
            )
        }
    }
}

@Composable
fun loadContactPhoto(
    contact: Contact
): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, contact) {
        withContext(Dispatchers.IO) {
            val stream = ContactsHelper.openContactPhotoStream(context, contact)
            val imageBitmap = stream?.let {
                BitmapFactory.decodeStream(it).asImageBitmap()
            }
            value = imageBitmap
        }
    }
}
