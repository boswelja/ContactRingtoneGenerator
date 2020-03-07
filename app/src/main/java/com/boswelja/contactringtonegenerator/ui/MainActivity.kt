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
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class MainActivity :
    AppCompatActivity(),
    TtsManager.UtteranceJobListener,
    ContactPickerDialog.DialogEventListener {

    private lateinit var ttsManager: TtsManager

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

    override fun onComplete() {
        Log.d("MainActivity", "Job completed")
    }

    override fun onJobError() {
        Log.d("MainActivity", "Job failed")
    }

    override fun onJobStart() {
        Log.d("MainActivity", "Job starting")
    }

    override fun onContactsSelected(selectedContacts: List<Contact>) {
        val selectedContactCount = selectedContacts.count()
        contactsSelectedView.text = resources.getQuantityString(R.plurals.selected_contacts_summary, 0, selectedContactCount)
        generateButton.isEnabled = ttsManager.isReady && selectedContactCount > 0
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
                    ttsManager.setContacts(selectedContacts)
                    ttsManager.startSynthesizing()
                }
            }
        }

        useNicknamesView.setOnCheckedChangeListener { _, isChecked ->
            ttsManager.useNicknames = isChecked
            contactPickerDialog.setUseNicknames(isChecked)
        }

        previewButton.apply {
            setOnClickListener {
                ttsManager.preview()
            }
        }

        ttsManager = TtsManager(this)
        ttsManager.registerTtsReadyListener(object :
            TtsManager.TtsReadyListener {
            override fun ttsReady() {
                setupVoicePickerSpinner()
                previewButton.isEnabled = true
            }
        })
        ttsManager.registerUtteranceListener(this)
        ttsManager.initTts()

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
        ttsManager.destroy()
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
                if (text.toString().contains("%NAME", true)) {
                    messageTextLayout.isErrorEnabled = false
                    ttsManager.setMessage(messageTextField.text.toString())
                } else {
                    messageTextLayout.isErrorEnabled = true
                    messageTextLayout.error = "Message should contain '%NAME' to use the name of the contact."
                }
            }
        }
        ttsManager.setMessage(messageTextField.text.toString())
    }

    private fun setupVoicePickerSpinner() {
        val voices = ttsManager.getAvailableVoices(Locale.getDefault())!!
        if (voices.isNotEmpty()) {
            voicePickerSpinner.apply {
                Log.d("VoicePickerSpinner", "Found ${voices.count()} voices")
                adapter =
                    VoiceSpinnerAdapter(
                        context,
                        voices
                    )
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val voice = voices[position]
                        ttsManager.setVoice(voice)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                setSelection(0)
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
        ttsManager.setSpeechRate(actualMultiplier)
    }

    companion object {
        private const val CONTACT_PICKER_PERMISSION_REQUEST_CODE = 1000
    }
}
