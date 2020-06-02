package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.databinding.FragmentRingtoneCreatorBinding
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorAvailableItemBinding
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.Utils.ALL_ITEMS
import com.google.android.material.chip.Chip

class RingtoneCreatorFragment : Fragment() {

    private val adapter = RingtoneCreatorAdapter()

    private val onAvailableItemClickListener = View.OnClickListener {
        if (it is Chip) {
            val item = ALL_ITEMS.first { item -> item.getLabel() == it.text }
            adapter.addItem(item)
            setMessageHasContent(true)
        }
    }

    private lateinit var binding: FragmentRingtoneCreatorBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRingtoneCreatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupAvailableMessageItems()
        setupMessageCreatorView()
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
        ALL_ITEMS.forEach {
            val chipBinding = RingtoneCreatorAvailableItemBinding.inflate(layoutInflater)
            chipBinding.root.apply {
                text = it.getLabel()
                setOnClickListener(onAvailableItemClickListener)
            }
            binding.availableItems.addView(chipBinding.root)
        }
    }

    private fun setMessageHasContent(hasContent: Boolean) {
        binding.apply {
            if (hasContent) {
                messageNoContentView.visibility = View.GONE
                nextButton.isEnabled = true
            } else {
                messageNoContentView.visibility = View.VISIBLE
                nextButton.isEnabled = false
            }
        }
    }
}
