package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.databinding.FragmentRingtoneCreatorBinding
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorAvailableItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID
import com.boswelja.contactringtonegenerator.ringtonegen.item.TextItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ui.WizardViewModel
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.ActionClickCallback
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.AdapterGestureHelper
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.RingtoneCreatorAdapter
import com.google.android.material.chip.Chip
import timber.log.Timber
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class RingtoneCreatorFragment : Fragment(), RingtoneCreatorAdapter.DataEventListener {

    private val wizardModel: WizardViewModel by activityViewModels()
    private val viewModel: RingtoneCreatorViewModel by viewModels()

    private val audioPickerLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { adapter.handleChooserResponse(requireContext(), it, pickerItemPosition) }

    private val ringtonePickerLauncher: ActivityResultLauncher<Int> = registerForActivityResult(
        PickRingtone()
    ) { adapter.handleChooserResponse(requireContext(), it, pickerItemPosition) }

    private val adapter = RingtoneCreatorAdapter(
        this,
        ActionClickCallback { id, position ->
            pickerItemPosition = position
            when (id) {
                ID.CUSTOM_AUDIO -> audioPickerLauncher.launch("audio/*")
                ID.SYSTEM_RINGTONE -> ringtonePickerLauncher.launch(RingtoneManager.TYPE_RINGTONE)
                else -> Timber.w("Unknown action clicked")
            }
        }
    )
    private var pickerItemPosition: Int = -1

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

    private lateinit var binding: FragmentRingtoneCreatorBinding

    override fun onItemAdded(item: StructureItem) {
        viewModel.addItem(item)
    }

    override fun onItemRemoved(position: Int) {
        viewModel.removeItemAtPosition(position)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        viewModel.moveItem(fromPosition, toPosition)
    }

    override fun onDataValidityChanged(isDataValid: Boolean) {
        viewModel.isDataValid.postValue(isDataValid)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRingtoneCreatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter.updateItems(viewModel.ringtoneStructure)
        setupAvailableMessageItems()
        setupMessageBuilderView()
        binding.nextButton.setOnClickListener {
            findNavController().navigate(RingtoneCreatorFragmentDirections.toLoadingFragment())
        }
        viewModel.isDataValid.observe(viewLifecycleOwner) {
            binding.nextButton.isEnabled = it
        }
        viewModel.isDataEmpty.observe(viewLifecycleOwner) {
            if (it) {
                binding.messageNoContentView.visibility = View.VISIBLE
            } else {
                binding.messageNoContentView.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        wizardModel.submitRingtoneStructure(viewModel.ringtoneStructure)
    }

    private fun setupMessageBuilderView() {
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
}
