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
import com.boswelja.contactringtonegenerator.ringtonegen.item.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID
import com.boswelja.contactringtonegenerator.ringtonegen.item.TextItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ui.WizardDataViewModel
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.ActionClickCallback
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.AdapterGestureHelper
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.RingtoneCreatorAdapter
import com.google.android.material.chip.Chip
import timber.log.Timber
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private const val CUSTOM_AUDIO_REQUEST_CODE = 62931
private const val SYSTEM_RINGTONE_REQUEST_CODE = 62932

class RingtoneCreatorFragment : Fragment(), RingtoneCreatorAdapter.DataEventListener {

    private val ringtoneStructure = ArrayList<StructureItem>()
    private val wizardDataModel: WizardDataViewModel by activityViewModels()
    private val adapter = RingtoneCreatorAdapter(
        this,
        ActionClickCallback { id, position ->
            pickerItemPosition = position
            when (id) {
                ID.CUSTOM_AUDIO -> startActivityForResult(customAudioPickerIntent, CUSTOM_AUDIO_REQUEST_CODE)
                ID.SYSTEM_RINGTONE -> startActivityForResult(systemRingtonePickerIntent, SYSTEM_RINGTONE_REQUEST_CODE)
                else -> Timber.w("Unknown action clicked")
            }
        }
    )
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
                ID.FIRST_NAME -> TextItem.FirstName()
                ID.CUSTOM_TEXT -> TextItem.Custom()
                ID.MIDDLE_NAME -> TextItem.MiddleName()
                ID.LAST_NAME -> TextItem.LastName()
                ID.PREFIX -> TextItem.NamePrefix()
                ID.SUFFIX -> TextItem.NameSuffix()
                ID.NICKNAME -> TextItem.Nickname()
                ID.CUSTOM_AUDIO -> AudioItem.File(requireContext())
                ID.SYSTEM_RINGTONE -> AudioItem.SystemRingtone(requireContext())
            }
            adapter.addItem(item)
        }
    }

    private val isDataEmpty: Boolean get() = ringtoneStructure.isEmpty()
    private var isDataValid: Boolean = true

    private lateinit var binding: FragmentRingtoneCreatorBinding

    override fun onItemAdded(item: StructureItem) {
        ringtoneStructure.add(item)
        updateNoDataViewVisibility()
        binding.messageBuilderView.smoothScrollToPosition(adapter.itemCount - 1)
    }

    override fun onItemRemoved(position: Int) {
        ringtoneStructure.removeAt(position)
        updateNoDataViewVisibility()
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        val item = ringtoneStructure.removeAt(fromPosition)
        ringtoneStructure.add(toPosition, item)
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
        adapter.updateItems(ringtoneStructure)
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
                adapter.handleChooserResponse(requireContext(), data?.data, pickerItemPosition)
            }
            SYSTEM_RINGTONE_REQUEST_CODE -> {
                val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                adapter.handleChooserResponse(requireContext(), uri, pickerItemPosition)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStop() {
        super.onStop()
        wizardDataModel.submitRingtoneStructure(ringtoneStructure)
    }

    private fun setupMessageCreatorView() {
        binding.messageBuilderView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@RingtoneCreatorFragment.adapter
            AdapterGestureHelper.attachToRecyclerView(this)
        }
    }

    private fun setupAvailableMessageItems() {
        createChipsFor(TextItem::class.sealedSubclasses)
        createChipsFor(AudioItem::class.sealedSubclasses)
    }

    private fun createChipsFor(list: List<KClass<out StructureItem>>) {
        list.forEach {
            val chipBinding = RingtoneCreatorAvailableItemBinding.inflate(layoutInflater)
            val item = it.createInstance()
            chipBinding.root.apply {
                id = item.id.ordinal
                setText(item.getLabelRes())
                setChipIconResource(item.getIconRes())
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
