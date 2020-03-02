package com.boswelja.contactringtonegenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.*
import androidx.core.widget.doOnTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class MainActivity :
    AppCompatActivity(),
    TtsManager.UtteranceJobListener {

    private lateinit var ttsManager: TtsManager

    private lateinit var contactSelectorView: RelativeLayout
    private lateinit var contactsSelectedView: AppCompatTextView

    private lateinit var voicePickerSpinner: AppCompatSpinner

    private lateinit var voiceSpeedSlider: AppCompatSeekBar
    private lateinit var voiceSpeedText: AppCompatTextView
    private lateinit var voiceSpeedReset: AppCompatImageView

    private lateinit var useNicknamesView: AppCompatCheckBox

    private lateinit var messageTextLayout: TextInputLayout
    private lateinit var messageTextField: AppCompatEditText

    private lateinit var generateButton: MaterialButton
    private lateinit var previewButton: MaterialButton

    private lateinit var contacts: List<Contact>
    private lateinit var selectedContacts: BooleanArray

    override fun onComplete() {
        Log.d("MainActivity", "Job completed")
    }

    override fun onJobError() {
        Log.d("MainActivity", "Job failed")
    }

    override fun onJobStart() {
        Log.d("MainActivity", "Job starting")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contacts = ContactManager.getContacts(this)
        selectedContacts = BooleanArray(contacts.size)
        Log.d("MainActivity", "Found ${contacts.size} contacts")

        setContentView(R.layout.activity_main)
        useNicknamesView = findViewById(R.id.use_nicknames_checkbox)

        generateButton = findViewById<MaterialButton>(R.id.generate_button).apply {
            setOnClickListener {
                ttsManager.useNicknames = useNicknamesView.isChecked
                ttsManager.setContacts(contacts.filterIndexed { index, _ -> selectedContacts[index] })
                ttsManager.startSynthesizing()
            }
        }

        previewButton = findViewById<MaterialButton>(R.id.preview_button).apply {
            setOnClickListener {
                ttsManager.preview()
            }
        }

        ttsManager = TtsManager(this)
        ttsManager.registerTtsReadyListener(object : TtsManager.TtsReadyListener {
            override fun ttsReady() {
                setupVoicePickerSpinner()
                generateButton.isEnabled = true
                previewButton.isEnabled = true
            }
        })
        ttsManager.registerUtteranceListener(this)
        ttsManager.initTts()

        setupMessageTextField()
        setupVoiceSpeedSlider()
        setupContactsPicker()
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.destroy()
    }

    private fun setupContactsPicker() {
        contactsSelectedView = findViewById(R.id.contact_counter)
        contactSelectorView = findViewById<RelativeLayout>(R.id.contact_selector).apply {
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity).apply {
                    setTitle("Pick Contacts")
                    setPositiveButton("Done") { _, _ -> }
                    setMultiChoiceItems(contacts.map {
                        if (useNicknamesView.isChecked) {
                            it.contactNickname ?: it.contactName
                        } else {
                            it.contactName
                        }
                    }.toTypedArray(), selectedContacts) { _, which, isChecked ->
                        selectedContacts[which] = isChecked
                        contactsSelectedView.text = "${selectedContacts.count { it }} contacts selected"
                    }
                }.also {
                    it.show()
                }
            }
        }
    }

    private fun setupMessageTextField() {
        messageTextLayout = findViewById(R.id.message_input_layout)
        messageTextField = findViewById<AppCompatEditText>(R.id.message_input_field).apply {
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
        voicePickerSpinner = findViewById<AppCompatSpinner>(R.id.voice_picker_spinner).apply {
            val voices = ttsManager.getAvailableVoices(Locale.getDefault())!!
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
                    ttsManager.setVoice(voice)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            setSelection(0)
        }
    }

    private fun setupVoiceSpeedSlider() {
        voiceSpeedText = findViewById(R.id.voice_speed_slider_text)
        voiceSpeedSlider = findViewById<AppCompatSeekBar>(R.id.voice_speed_slider).apply {
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
        voiceSpeedReset = findViewById<AppCompatImageView>(R.id.voice_speed_reset).apply {
            setOnClickListener {
                voiceSpeedSlider.progress = 5
                voiceSpeedSliderChange(1.0f)
            }
        }
    }

    private fun voiceSpeedSliderChange(actualMultiplier: Float) {
        voiceSpeedText.text = "${actualMultiplier}x"
        ttsManager.setSpeechRate(actualMultiplier)
    }
}
