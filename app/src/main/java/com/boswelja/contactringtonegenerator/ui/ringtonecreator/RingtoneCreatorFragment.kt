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
import com.boswelja.contactringtonegenerator.ringtonegen.item.BaseItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.ContactName
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID
import com.boswelja.contactringtonegenerator.ringtonegen.item.TextItem
import com.boswelja.contactringtonegenerator.ui.WizardDataViewModel
import com.google.android.material.chip.Chip

class RingtoneCreatorFragment : Fragment(), RingtoneCreatorAdapter.DataEventListener {

    private val dataModel: WizardDataViewModel by activityViewModels()
    private val adapter = RingtoneCreatorAdapter(this)

    private val onAvailableItemClickListener = View.OnClickListener {
        if (it is Chip) {
            val item = when (ID.values().first { item -> item.id == it.id }) {
                ID.CONTACT_NAME -> ContactName()
                ID.TEXT_ITEM -> TextItem()
            }
            adapter.addItem(item)
        }
    }

    private var isDataEmpty: Boolean = true
    private var isDataValid: Boolean = true

    private lateinit var binding: FragmentRingtoneCreatorBinding

    override fun onItemAdded(item: BaseItem) {
        isDataEmpty = false
        updateNoDataViewVisibility()
        binding.messageBuilderView.smoothScrollToPosition(adapter.itemCount - 1)
        if (isDataValid) dataModel.setRingtoneStructure(adapter.getItems())
    }

    override fun onItemRemoved(position: Int, isEmpty: Boolean) {
        isDataEmpty = isEmpty
        updateNoDataViewVisibility()
        if (isDataValid) dataModel.setRingtoneStructure(adapter.getItems())
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {

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
                    ID.CONTACT_NAME -> {
                        ContactName().getLabel()
                    }
                    ID.TEXT_ITEM -> {
                        TextItem().getLabel()
                    }
                }
                id = it.id
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
