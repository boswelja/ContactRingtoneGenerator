package com.boswelja.contactringtonegenerator.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.StringJoinerCompat
import com.boswelja.contactringtonegenerator.databinding.SheetPermissionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PermissionSheet : BottomSheetDialogFragment() {

    private val permissions =
        arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            dismiss()
        } else {
            checkMissingPermissions()
        }
    }

    private lateinit var binding: SheetPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.grantButton.setOnClickListener {
            if (permissions.any { shouldShowRequestPermissionRationale(it) }) {
                launchApplicationSettings()
            } else {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkMissingPermissions()
    }

    private fun checkMissingPermissions() {
        val permissionRequestTextBuilder = StringJoinerCompat("\n")
        if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
            permissionRequestTextBuilder.add(getString(R.string.permission_missing_read_contacts))
        }
        if (!hasPermission(Manifest.permission.WRITE_CONTACTS)) {
            permissionRequestTextBuilder.add(getString(R.string.permission_missing_write_contacts))
        }
        if (permissionRequestTextBuilder.length > 0) {
            binding.permissionDescriptionView.text = permissionRequestTextBuilder.toString()
        } else {
            dismiss()
        }
    }

    private fun launchApplicationSettings() {
        Toast.makeText(
            requireContext(),
            "Please grant the required permission here",
            Toast.LENGTH_LONG
        ).show()
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }.also {
            startActivity(it)
        }
    }

    private fun hasPermission(permission: String) =
        requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
