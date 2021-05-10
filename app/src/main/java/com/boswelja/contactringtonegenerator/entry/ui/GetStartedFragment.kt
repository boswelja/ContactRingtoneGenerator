package com.boswelja.contactringtonegenerator.entry.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.PermissionSheet
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.common.ui.AppTheme

class GetStartedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GetStartedScreen()
            }
        }
    }

    @Composable
    @Preview
    fun GetStartedScreen() {
        AppTheme {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(top = 64.dp, bottom = 64.dp)
            ) {
                AppInfo()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f).fillMaxSize()
                ) {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.get_started)) },
                        icon = { Icon(Icons.Outlined.NavigateNext, null) },
                        onClick = {
                            getStarted()
                        }
                    )
                }
                OutlinedButton(
                    onClick = { navigateToSettings() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Outlined.Settings, null)
                    Text(stringResource(R.string.settings_title))
                }
            }
        }
    }

    @Composable
    @Preview
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

    /**
     * Navigate to Settings.
     */
    private fun navigateToSettings() {
        findNavController().navigate(GetStartedFragmentDirections.toSettingsFragment())
    }

    /**
     * Starts the generator flow, or asks for permission if missing first.
     */
    private fun getStarted() {
        if (hasContactPermissions()) {
            findNavController().navigate(GetStartedFragmentDirections.toContactPickerFragment())
        } else {
            PermissionSheet().show(childFragmentManager, "PermissionSheet")
        }
    }

    /**
     * Checks whether this app has READ_CONTACTS or WRITE_CONTACTS.
     * @return true if we have both permissions, false otherwise.
     */
    private fun hasContactPermissions(): Boolean =
        context?.checkSelfPermission(
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED &&
            context?.checkSelfPermission(
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
}
