package com.boswelja.contactringtonegenerator.contactpicker.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.common.ui.NextButton
import com.boswelja.contactringtonegenerator.contactpicker.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
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

    var currentQuery by remember {
        mutableStateOf("")
    }

    val visibleContacts by remember(allContacts, currentQuery) {
        derivedStateOf {
            allContacts?.filter { it.second.displayName.contains(currentQuery, ignoreCase = true) }
        }
    }

    Box(modifier) {
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = visibleContacts != null
        ) { hasLoaded ->
            if (hasLoaded) {
                ContactsList(
                    modifier = Modifier.fillMaxSize(),
                    contentPaddingValues = PaddingValues(bottom = 72.dp),
                    contacts = visibleContacts!!,
                    selectedContacts = selectedContacts,
                    onContactSelectionChanged = onContactSelectionChanged,
                    searchQuery = currentQuery,
                    onSearchQueryChanged = { currentQuery = it }
                )
            } else {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        NextButton(
            enabled = selectedContacts.isNotEmpty(),
            onClick = onNavigateNext
        )
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun ContactsList(
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(),
    contacts: List<Pair<ImageBitmap?, Contact>>,
    selectedContacts: Collection<String>,
    onContactSelectionChanged: (Contact, Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPaddingValues
    ) {
        stickyHeader {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                searchQuery = searchQuery,
                onQueryChanged = onSearchQueryChanged
            )
        }
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

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    elevation: Dp = 2.dp,
    searchQuery: String,
    onQueryChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Surface(
        elevation = elevation
    ) {
        OutlinedTextField(
            modifier = modifier,
            value = searchQuery,
            onValueChange = onQueryChanged,
            singleLine = true,
            keyboardActions = KeyboardActions {
                // Clear focus on action
                focusManager.clearFocus()
            },
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(Icons.Default.Clear, stringResource(R.string.search_clear))
                }
            }
        )
    }
}
