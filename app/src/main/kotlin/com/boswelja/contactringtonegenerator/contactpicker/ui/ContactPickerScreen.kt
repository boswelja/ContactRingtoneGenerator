package com.boswelja.contactringtonegenerator.contactpicker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.common.LocalSearchComposition
import com.boswelja.contactringtonegenerator.contactpicker.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun ContactPickerScreen(
    modifier: Modifier = Modifier,
    selectedContacts: Collection<String>,
    onContactSelectionChanged: (Contact, Boolean) -> Unit,
    onNavigateNext: () -> Unit
) {
    val viewModel: ContactPickerViewModel = viewModel()
    val allContacts by viewModel.allContacts.collectAsState(
        null,
        Dispatchers.IO
    )

    val currentQuery = LocalSearchComposition.current

    val visibleContacts by remember(allContacts, currentQuery) {
        derivedStateOf {
            allContacts?.filter { it.second.displayName.contains(currentQuery, ignoreCase = true) }
        }
    }

    Box(modifier) {
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = visibleContacts != null
        ) {
            if (it) {
                ContactsList(
                    modifier = Modifier.fillMaxSize(),
                    contentPaddingValues = PaddingValues(top = 8.dp, bottom = 72.dp),
                    contacts = visibleContacts!!,
                    selectedContacts = selectedContacts,
                    onContactSelectionChanged = onContactSelectionChanged
                )
            } else {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            visible = selectedContacts.isNotEmpty()
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
    contacts: List<Pair<ImageBitmap?, Contact>>,
    selectedContacts: Collection<String>,
    onContactSelectionChanged: (Contact, Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPaddingValues
    ) {
        items(
            items = contacts,
            key = { it.second.lookupKey }
        ) { (image, contact) ->
            val iconModifier = Modifier.size(48.dp)
            val iconTint = LocalContentColor.current.copy(alpha = ContentAlpha.medium)

            var selected by remember {
                mutableStateOf(selectedContacts.contains(contact.lookupKey))
            }

            ListItem(
                text = { Text(contact.displayName) },
                icon = {
                    if (image != null) {
                        Image(
                            bitmap = image,
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
