package com.boswelja.contactringtonegenerator.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.doOnTextChanged
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class MainActivity :
    AppCompatActivity(),
    ContactPickerDialog.DialogEventListener {

    private lateinit var ringtoneGenerator: RingtoneGenerator

    private lateinit var contactSelectorView: RelativeLayout
    private lateinit var contactsSelectedView: AppCompatTextView

    private lateinit var useNicknamesView: AppCompatCheckBox

    private lateinit var voicePickerView: LinearLayoutCompat
    private lateinit var voicePickerSpinner: AppCompatSpinner

    private lateinit var voiceSpeedSlider: AppCompatSeekBar
    private lateinit var voiceSpeedText: AppCompatTextView
    private lateinit var voiceSpeedReset: AppCompatImageView

    private lateinit var messageTextLayout: TextInputLayout
    private lateinit var messageTextField: AppCompatEditText

    private lateinit var generateButton: MaterialButton
    private lateinit var previewButton: MaterialButton

    private lateinit var contactPickerDialog: ContactPickerDialog

    override fun onContactsSelected(selectedContacts: List<Contact>) {
        val selectedContactCount = selectedContacts.count()
        contactsSelectedView.text = resources.getQuantityString(R.plurals.selected_contacts_summary, 0, selectedContactCount)
        ringtoneGenerator.setContacts(selectedContacts)
        generateButton.isEnabled = ringtoneGenerator.isReady && selectedContactCount > 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contactPickerDialog = ContactPickerDialog()
        contactPickerDialog.dialogEventListeners.add(this)

        setContentView(R.layout.activity_main)
        initViews()

        generateButton.apply {
            setOnClickListener {
                val selectedContacts = contactPickerDialog.getSelectedContacts()
                if (!selectedContacts.isNullOrEmpty()) {
                    ringtoneGenerator.setContacts(selectedContacts)
                    ringtoneGenerator.generate()
                }
            }
        }

        useNicknamesView.setOnCheckedChangeListener { _, isChecked ->
            ringtoneGenerator.useNicknames = isChecked
            contactPickerDialog.setUseNicknames(isChecked)
        }

        previewButton.apply {
            setOnClickListener {
                ringtoneGenerator.preview()
            }
        }

        ringtoneGenerator = RingtoneGenerator(this)
        ringtoneGenerator.addEventListener(object : RingtoneGenerator.EventListener {
            override fun onReady() {
                setupVoicePickerSpinner()
                previewButton.isEnabled = true
            }
        })
        ringtoneGenerator.init()

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
                    PackageManager.PERMISSION_GRANTED -> contactPickerDialog.show(supportFragmentManager)
                    PackageManager.PERMISSION_DENIED -> {
                        // Permission denied, explain why we need it
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun initViews() {
        contactsSelectedView = findViewById(R.id.contact_selector_summary)
        contactSelectorView = findViewById(R.id.contact_selector)

        useNicknamesView = findViewById(R.id.use_nicknames_checkbox)

        voicePickerView = findViewById(R.id.voice_picker_view)
        voicePickerSpinner = findViewById(R.id.voice_picker_spinner)

        voiceSpeedText = findViewById(R.id.voice_speed_slider_text)
        voiceSpeedSlider = findViewById(R.id.voice_speed_slider)
        voiceSpeedReset = findViewById(R.id.voice_speed_reset)

        messageTextLayout = findViewById(R.id.message_input_layout)
        messageTextField = findViewById(R.id.message_input_field)

        generateButton = findViewById(R.id.generate_button)
        previewButton = findViewById(R.id.preview_button)
    }

    private fun setupContactsPicker() {
        contactsSelectedView.text = resources.getQuantityString(R.plurals.selected_contacts_summary, 0, contactPickerDialog.getSelectedContacts().count())
        contactSelectorView.apply {
            setOnClickListener {
                if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), CONTACT_PICKER_PERMISSION_REQUEST_CODE)
                } else {
                    contactPickerDialog.show(supportFragmentManager)
                }
            }
        }
    }

    private fun setupMessageTextField() {
        messageTextField.apply {
            doOnTextChanged { text, _, _, _ ->
                if (ringtoneGenerator.setRingtoneMessage(text.toString())) {
                    messageTextLayout.isErrorEnabled = false
                } else {
                    messageTextLayout.isErrorEnabled = true
                    messageTextLayout.error = "Message should contain '%NAME' to use the name of the contact."
                }
            }
        }
        ringtoneGenerator.setRingtoneMessage(messageTextField.text.toString())
    }

    private fun setupVoicePickerSpinner() {
        val voices = ringtoneGenerator.getAvailableVoices(Locale.getDefault())!!
        if (voices.isNotEmpty()) {
            voicePickerSpinner.apply {
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
            voicePickerView.visibility = View.VISIBLE
        } else {
            voicePickerView.visibility = View.GONE
        }

    }

    private fun setupVoiceSpeedSlider() {
        voiceSpeedSliderChange(1.0f)
        voiceSpeedSlider.apply {
            setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
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
        }
        voiceSpeedReset.apply {
            setOnClickListener {
                voiceSpeedSlider.progress = 5
                voiceSpeedSliderChange(1.0f)
            }
        }
    }

    private fun voiceSpeedSliderChange(actualMultiplier: Float) {
        voiceSpeedText.text = getString(R.string.speech_rate_multiplier_text, actualMultiplier)
        ringtoneGenerator.setSpeechRate(actualMultiplier)
    }

    companion object {
        private const val CONTACT_PICKER_PERMISSION_REQUEST_CODE = 1000
    }
}
