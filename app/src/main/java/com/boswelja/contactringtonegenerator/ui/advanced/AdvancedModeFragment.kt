package com.boswelja.contactringtonegenerator.ui.advanced

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.FragmentAdvancedModeBinding
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import java.util.Locale

class AdvancedModeFragment : Fragment(), ContactPickerDialog.DialogEventListener {

    private lateinit var ringtoneGenerator: RingtoneGenerator
    private lateinit var contactPickerDialog: ContactPickerDialog
    private lateinit var binding: FragmentAdvancedModeBinding

    override fun onContactsSelected(selectedContacts: List<Contact>) {
        val selectedContactCount = selectedContacts.count()
        ringtoneGenerator.setContacts(selectedContacts)
        binding.apply {
            contactSelectorSummary.text = resources.getQuantityString(R.plurals.selected_contacts_summary, 0, selectedContactCount)
            generateButton.isEnabled = ringtoneGenerator.isReady && selectedContactCount > 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactPickerDialog = ContactPickerDialog()
        contactPickerDialog.dialogEventListeners.add(this)
        ringtoneGenerator = RingtoneGenerator(requireContext())
        ringtoneGenerator.addEventListener(object : RingtoneGenerator.EventListener {
            override fun onReady() {
                setupVoicePickerSpinner()
                binding.previewButton.isEnabled = true
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAdvancedModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ringtoneGenerator.init()
        binding.apply {
            generateButton.apply {
                setOnClickListener {
                    val selectedContacts = contactPickerDialog.getSelectedContacts()
                    if (!selectedContacts.isNullOrEmpty()) {
                        ringtoneGenerator.setContacts(selectedContacts)
                        ringtoneGenerator.generate()
                    }
                }
            }

            useNicknamesCheckbox.setOnCheckedChangeListener { _, isChecked ->
                ringtoneGenerator.useNicknames = isChecked
                contactPickerDialog.setUseNicknames(isChecked)
            }

            previewButton.setOnClickListener { ringtoneGenerator.preview() }
        }

        setupMessageTextField()
        setupVoiceSpeedSlider()
        setupContactsPicker()
    }

    override fun onResume() {
        super.onResume()
        contactPickerDialog.wantsContactUpdate = true
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtoneGenerator.destroy()
        contactPickerDialog.dialogEventListeners.remove(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CONTACT_PICKER_PERMISSION_REQUEST_CODE -> {
                when (grantResults[0]) {
                    PackageManager.PERMISSION_GRANTED -> contactPickerDialog.show(parentFragmentManager)
                    PackageManager.PERMISSION_DENIED -> {
                        // Permission denied, explain why we need it
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setupContactsPicker() {
        binding.apply {
            contactSelectorSummary.text = resources.getQuantityString(R.plurals.selected_contacts_summary, 0, contactPickerDialog.getSelectedContacts().count())
            contactSelector.setOnClickListener {
                if (context?.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), CONTACT_PICKER_PERMISSION_REQUEST_CODE)
                } else {
                    contactPickerDialog.show(parentFragmentManager)
                }
            }
        }
    }

    private fun setupMessageTextField() {
        binding.apply {
            messageInputField.doOnTextChanged { text, _, _, _ ->
                if (ringtoneGenerator.setRingtoneMessage(text.toString())) {
                    messageInputLayout.isErrorEnabled = false
                } else {
                    messageInputLayout.isErrorEnabled = true
                    messageInputLayout.error = "Message should contain '%NAME' to use the name of the contact."
                }
            }
        }
        ringtoneGenerator.setRingtoneMessage(binding.messageInputField.text.toString())
    }

    private fun setupVoicePickerSpinner() {
        val voices = ringtoneGenerator.getAvailableVoices(Locale.getDefault())!!
        if (voices.isNotEmpty()) {
            binding.voicePickerSpinner.apply {
                Log.d("VoicePickerSpinner", "Found ${voices.count()} voices")
                adapter = VoiceSpinnerAdapter(context, voices)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                    ) {
                        val voice = voices[position]
                        ringtoneGenerator.setVoice(voice)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                setSelection(0)
                visibility = View.VISIBLE
            }
            binding.voicePickerView.visibility = View.VISIBLE
        } else {
            binding.voicePickerView.visibility = View.GONE
        }

    }

    private fun setupVoiceSpeedSlider() {
        voiceSpeedSliderChange(1.0f)
        binding.apply {
            voiceSpeedSlider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                ) {
                    val actualMultiplier = (progress + 5) / 10.0f
                    voiceSpeedSliderChange(actualMultiplier)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            })
            voiceSpeedReset.setOnClickListener {
                voiceSpeedSlider.progress = 5
                voiceSpeedSliderChange(1.0f)
            }
        }

    }

    private fun voiceSpeedSliderChange(actualMultiplier: Float) {
        binding.voiceSpeedSliderText.text = getString(R.string.speech_rate_multiplier_text, actualMultiplier)
        ringtoneGenerator.setSpeechRate(actualMultiplier)
    }

    companion object {
        private const val CONTACT_PICKER_PERMISSION_REQUEST_CODE = 1000
    }
}