package com.boswelja.contactringtonegenerator.contactpicker.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.MainActivity
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.WizardViewModel
import com.boswelja.contactringtonegenerator.common.ui.AppTheme
import com.boswelja.contactringtonegenerator.contactpicker.Contact
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

class ContactPickerFragment : Fragment() {

    private val wizardModel: WizardViewModel by activityViewModels()

    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel: ContactsViewModel = viewModel()
                AppTheme {
                    Scaffold(
                        floatingActionButton = {
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.next)) },
                                icon = { Icon(Icons.Outlined.NavigateNext, null) },
                                onClick = {
                                    wizardModel.submitSelectedContacts(
                                        viewModel.selectedContacts
                                    )
                                    findNavController().navigate(
                                        ContactPickerFragmentDirections.toRingtoneCreatorFragment()
                                    )
                                }
                            )
                        }
                    ) {
                        var searchQuery by remember { mutableStateOf("") }
                        var allSelected by remember { mutableStateOf(false) }
                        val contacts by viewModel.adapterContacts.observeAsState()
                        Column {
                            ListHeader(
                                searchQuery = searchQuery,
                                onSearchQueryChanged = {
                                    searchQuery = it
                                    viewModel.searchQuery.postValue(searchQuery)
                                },
                                allSelected = allSelected,
                                onAllSelectedChange = {
                                    allSelected = it
                                    contacts?.forEach { contact ->
                                        if (
                                            allSelected &&
                                            !viewModel.selectedContacts.contains(contact)
                                        ) {
                                            viewModel.selectedContacts.add(contact)
                                        } else if (!allSelected) {
                                            viewModel.selectedContacts.remove(contact)
                                        }
                                    }
                                }
                            )
                            ContactsList(
                                contacts = contacts,
                                selectedContacts = viewModel.selectedContacts,
                                onContactSelectionChanged = { contact, isSelected ->
                                    Timber.d(
                                        "Selection changed for %s, is now %s",
                                        contact,
                                        isSelected
                                    )
                                    if (isSelected) viewModel.selectedContacts.add(contact)
                                    else viewModel.selectedContacts.remove(contact)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        removeSubtitle()
    }

    private fun removeSubtitle() {
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.setSubtitle(null)
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = {
            Text(stringResource(R.string.search_hint))
        },
        leadingIcon = {
            Icon(Icons.Outlined.Search, stringResource(R.string.search_hint))
        },
        modifier = modifier
    )
}

@Composable
fun ListHeader(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    allSelected: Boolean,
    onAllSelectedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
            Modifier.weight(1f)
        )
        Checkbox(
            checked = allSelected,
            onCheckedChange = onAllSelectedChange,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun ContactsList(
    contacts: List<Contact>?,
    selectedContacts: List<Contact>,
    onContactSelectionChanged: (Contact, Boolean) -> Unit
) {
    val context = LocalContext.current
    contacts?.let {
        LazyColumn {
            items(contacts) { contact ->
                val selected = selectedContacts.contains(contact)
                ListItem(
                    text = { Text(contact.displayName) },
                    icon = {
                        ContactsHelper.openContactPhotoStream(context, contact)?.let {
                            val imageBitmap = BitmapFactory.decodeStream(it).asImageBitmap()
                            it.close()
                            Image(imageBitmap, null, Modifier.clip(CircleShape))
                        } ?: Icon(Icons.Outlined.AccountCircle, null)
                    },
                    trailing = {
                        Checkbox(checked = selected, onCheckedChange = null)
                    },
                    modifier = Modifier.clickable {
                        onContactSelectionChanged(contact, !selected)
                    }
                )
            }
        }
    }
}
