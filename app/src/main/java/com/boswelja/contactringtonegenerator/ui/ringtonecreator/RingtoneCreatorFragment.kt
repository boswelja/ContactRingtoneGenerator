package com.boswelja.contactringtonegenerator.ui.ringtonecreator

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
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.FirstName
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID
import com.boswelja.contactringtonegenerator.ringtonegen.item.LastName
import com.boswelja.contactringtonegenerator.ringtonegen.item.MiddleName
import com.boswelja.contactringtonegenerator.ringtonegen.item.NamePrefix
import com.boswelja.contactringtonegenerator.ringtonegen.item.NameSuffix
import com.boswelja.contactringtonegenerator.ringtonegen.item.Nickname
import com.boswelja.contactringtonegenerator.ringtonegen.item.TextItem
import com.boswelja.contactringtonegenerator.ui.WizardDataViewModel
import com.google.android.material.chip.Chip

class RingtoneCreatorFragment : Fragment(), RingtoneCreatorAdapter.DataEventListener {

    private val dataModel: WizardDataViewModel by activityViewModels()
    private val adapter = RingtoneCreatorAdapter(this)

    private val onAvailableItemClickListener = View.OnClickListener {
        if (it is Chip) {
            val item = when (ID.values().first { item -> item.ordinal == it.id }) {
                ID.FIRST_NAME -> FirstName()
                ID.TEXT_ITEM -> TextItem()
                ID.MIDDLE_NAME -> MiddleName()
                ID.LAST_NAME -> LastName()
                ID.PREFIX -> NamePrefix()
                ID.SUFFIX -> NameSuffix()
                ID.NICKNAME -> Nickname()
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
                // TODO There must be a better way of getting the label for an item
                text = when (it) {
                    ID.FIRST_NAME -> {
                        FirstName().getLabel()
                    }
                    ID.TEXT_ITEM -> {
                        TextItem().getLabel()
                    }
                    ID.MIDDLE_NAME -> MiddleName().getLabel()
                    ID.LAST_NAME -> LastName().getLabel()
                    ID.PREFIX -> NamePrefix().getLabel()
                    ID.SUFFIX -> NameSuffix().getLabel()
                    ID.NICKNAME -> Nickname().getLabel()
                }
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
