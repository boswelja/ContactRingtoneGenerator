package com.boswelja.contactringtonegenerator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.FragmentPermissionBinding

class PermissionFragment : Fragment() {

    private val args: PermissionFragmentArgs by navArgs()
    private lateinit var permission: String

    private lateinit var binding: FragmentPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permission = args.permission
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            when (permission) {
                Manifest.permission.WRITE_CONTACTS -> {
                    permissionDescriptionView.setText(R.string.contact_picker_missing_permission)
                }
            }
            grantButton.setOnClickListener {
                requestPermissions(arrayOf(permission), REQUEST_PERMISSION_RESULT)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_RESULT -> {
                val granted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    when (permission) {
                        Manifest.permission.WRITE_CONTACTS -> {
                            val action = PermissionFragmentDirections.toContactPickerFragment()
                            findNavController().navigate(action)
                        }
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_RESULT = 123
    }
}