package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.databinding.FragmentRingtoneCreatorBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureChoice
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ui.WizardViewModel
import com.boswelja.contactringtonegenerator.ui.common.AppTheme
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.ActionClickCallback
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.AdapterGestureHelper
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.RingtoneCreatorAdapter
import timber.log.Timber

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
        binding.chipScroller.setContent {
            AppTheme {
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(StructureChoice.ALL) { choice ->
                        ItemChip(choice) {
                            adapter.addItem(it.createStructureItem())
                        }
                    }
                }
            }
        }
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

    @Composable
    fun ItemChip(
        item: StructureChoice,
        onClick: (StructureChoice) -> Unit
    ) {
        Surface(
            color = Color.LightGray,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                Modifier
                    .clickable { onClick(item) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(item.icon, null)
                Text(stringResource(item.textRes))
            }
        }
    }
}
