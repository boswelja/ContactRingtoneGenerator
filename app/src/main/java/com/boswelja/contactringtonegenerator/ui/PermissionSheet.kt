package com.boswelja.contactringtonegenerator.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.StringJoinerCompat
import com.boswelja.contactringtonegenerator.databinding.SheetPermissionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PermissionSheet : BottomSheetDialogFragment() {

    private val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)

    private lateinit var binding: SheetPermissionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SheetPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateMissingPermissionDescription()

        binding.grantButton.setOnClickListener {
            if (permissions.any { shouldShowRequestPermissionRationale(it)}) {
                launchApplicationSettings()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                dismiss()
            } else {
                Log.d("PermissionSheet", "Permissions denied")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.all { hasPermission(it) }) {
                dismiss()
            } else {
                updateMissingPermissionDescription()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateMissingPermissionDescription() {
        val permissionRequestTextBuilder = StringJoinerCompat("\n")
        if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
            permissionRequestTextBuilder.add(getString(R.string.permission_missing_read_contacts))
        }
        if (!hasPermission(Manifest.permission.WRITE_CONTACTS)) {
            permissionRequestTextBuilder.add(getString(R.string.permission_missing_write_contacts))
        }
        binding.permissionDescriptionView.text = permissionRequestTextBuilder.toString()
    }

    private fun launchApplicationSettings() {
        Toast.makeText(requireContext(), "Please grant the required permission here", Toast.LENGTH_LONG).show()
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }.also {
            startActivityForResult(it, PERMISSION_REQUEST_CODE)
        }
    }

    private fun hasPermission(permission: String) =
            requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}