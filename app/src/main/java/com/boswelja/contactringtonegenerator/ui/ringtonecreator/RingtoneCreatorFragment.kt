package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.databinding.FragmentRingtoneCreatorBinding
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorAvailableItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomAudio
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomText
import com.boswelja.contactringtonegenerator.ringtonegen.item.FirstName
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID
import com.boswelja.contactringtonegenerator.ringtonegen.item.LastName
import com.boswelja.contactringtonegenerator.ringtonegen.item.MiddleName
import com.boswelja.contactringtonegenerator.ringtonegen.item.NamePrefix
import com.boswelja.contactringtonegenerator.ringtonegen.item.NameSuffix
import com.boswelja.contactringtonegenerator.ringtonegen.item.Nickname
import com.boswelja.contactringtonegenerator.ringtonegen.item.SystemRingtone
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.TextItem
import com.boswelja.contactringtonegenerator.ui.WizardDataViewModel
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.ActionClickCallback
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.AdapterGestureHelper
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.RingtoneCreatorAdapter
import com.google.android.material.chip.Chip
import timber.log.Timber

private const val CUSTOM_AUDIO_REQUEST_CODE = 62931
private const val SYSTEM_RINGTONE_REQUEST_CODE = 62932

class RingtoneCreatorFragment : Fragment(), RingtoneCreatorAdapter.DataEventListener {

    private val dataModel: WizardDataViewModel by activityViewModels()
    private val adapter = RingtoneCreatorAdapter(this, ActionClickCallback { id, position ->
        pickerItemPosition = position
        when (id) {
            ID.CUSTOM_AUDIO -> startActivityForResult(customAudioPickerIntent, CUSTOM_AUDIO_REQUEST_CODE)
            ID.SYSTEM_RINGTONE -> startActivityForResult(systemRingtonePickerIntent, SYSTEM_RINGTONE_REQUEST_CODE)
            else -> Timber.w("Unknown action clicked")
        }
    })
    private var pickerItemPosition: Int = -1

    private val customAudioPickerIntent by lazy {
        Intent.createChooser(
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "audio/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                },
                "Pick an audio file"
        )
    }

    private val systemRingtonePickerIntent by lazy {
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
        }
    }

    private val onAvailableItemClickListener = View.OnClickListener {
        if (it is Chip) {
            val item = when (ID.values().first { item -> item.ordinal == it.id }) {
                ID.FIRST_NAME -> FirstName()
                ID.CUSTOM_TEXT -> CustomText()
                ID.MIDDLE_NAME -> MiddleName()
                ID.LAST_NAME -> LastName()
                ID.PREFIX -> NamePrefix()
                ID.SUFFIX -> NameSuffix()
                ID.NICKNAME -> Nickname()
                ID.CUSTOM_AUDIO -> CustomAudio()
                ID.SYSTEM_RINGTONE -> SystemRingtone()
            }
            adapter.addItem(item)
        }
    }

    private val isDataEmpty: Boolean get() = dataModel.ringtoneStructure.isEmpty()
    private var isDataValid: Boolean = true

    private lateinit var binding: FragmentRingtoneCreatorBinding

    override fun onItemAdded(item: StructureItem) {
        dataModel.ringtoneStructure.add(item)
        updateNoDataViewVisibility()
        binding.messageBuilderView.smoothScrollToPosition(adapter.itemCount - 1)
    }

    override fun onItemRemoved(position: Int) {
        dataModel.ringtoneStructure.removeAt(position)
        updateNoDataViewVisibility()
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        val item = dataModel.ringtoneStructure.removeAt(fromPosition)
        dataModel.ringtoneStructure.add(toPosition, item)
    }

    override fun onDataValidityChanged(isDataValid: Boolean) {
        this.isDataValid = isDataValid
        updateNextButtonEnabled()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRingtoneCreatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter.updateItems(dataModel.ringtoneStructure)
        setupAvailableMessageItems()
        setupMessageCreatorView()
        updateNextButtonEnabled()
        updateNoDataViewVisibility()
        binding.nextButton.setOnClickListener {
            findNavController().navigate(RingtoneCreatorFragmentDirections.toLoadingFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CUSTOM_AUDIO_REQUEST_CODE -> {
                adapter.handleChooserResponse(data?.data, pickerItemPosition)
            }
            SYSTEM_RINGTONE_REQUEST_CODE -> {
                val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                adapter.handleChooserResponse(uri, pickerItemPosition)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setupMessageCreatorView() {
        binding.messageBuilderView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@RingtoneCreatorFragment.adapter
            AdapterGestureHelper.attachToRecyclerView(this)
        }
    }

    private fun setupAvailableMessageItems() {
        ID.values().forEach {
            val chipBinding = RingtoneCreatorAvailableItemBinding.inflate(layoutInflater)
            chipBinding.root.apply {
                setText(
                    when (it) {
                        ID.FIRST_NAME -> FirstName.labelRes
                        ID.CUSTOM_TEXT -> CustomText.labelRes
                        ID.MIDDLE_NAME -> MiddleName.labelRes
                        ID.LAST_NAME -> LastName.labelRes
                        ID.PREFIX -> NamePrefix.labelRes
                        ID.SUFFIX -> NameSuffix.labelRes
                        ID.NICKNAME -> Nickname.labelRes
                        ID.CUSTOM_AUDIO -> CustomAudio.labelRes
                        ID.SYSTEM_RINGTONE -> SystemRingtone.labelRes
                    }
                )
                setChipIconResource(
                    when (it) {
                        ID.FIRST_NAME,
                        ID.CUSTOM_TEXT,
                        ID.MIDDLE_NAME,
                        ID.LAST_NAME,
                        ID.PREFIX,
                        ID.SUFFIX,
                        ID.NICKNAME
                        -> TextItem.iconRes
                        ID.CUSTOM_AUDIO,
                        ID.SYSTEM_RINGTONE
                        -> AudioItem.iconRes
                    }
                )
                id = it.ordinal
                setOnClickListener(onAvailableItemClickListener)
            }
            binding.availableItems.addView(chipBinding.root)
        }
    }

    private fun updateNoDataViewVisibility() {
        val shouldShow = isDataEmpty
        binding.apply {
            if (shouldShow) {
                messageNoContentView.visibility = View.VISIBLE
            } else {
                messageNoContentView.visibility = View.GONE
            }
        }
    }

    private fun updateNextButtonEnabled() {
        val shouldEnable = !isDataEmpty && isDataValid
        binding.nextButton.isEnabled = shouldEnable
    }
}
