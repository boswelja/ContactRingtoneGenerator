package com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorCustomAudioWidgetBinding
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.ActionClickCallback
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.RingtoneCreatorAdapter

class CustomAudioViewHolder private constructor(
    private val adapter: RingtoneCreatorAdapter,
    private val widgetBinding: RingtoneCreatorCustomAudioWidgetBinding,
    binding: RingtoneCreatorItemBinding
) : BaseViewHolder(binding) {

    init {
        initWidgetView()
    }

    override fun createWidgetView(): View {
        return widgetBinding.root
    }

    // TODO Find a better way to do this, ignoring the abstract bind function feels wrong
    fun bind(item: StructureItem, clickCallback: ActionClickCallback) {
        if (item is AudioItem) {
            widgetBinding.apply {
                fileNameView.text = item.displayText
                browseButton.setOnClickListener { clickCallback.onClick(item, adapterPosition) }
            }
            adapter.setIsDataValid(adapterPosition, item.audioUri != null)
        }
    }

    override fun bind(item: StructureItem) {
        TODO("Not yet implemented")
    }

    companion object {
        fun from(adapter: RingtoneCreatorAdapter, parent: ViewGroup): CustomAudioViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = RingtoneCreatorItemBinding.inflate(layoutInflater, parent, false)
            val widgetBinding = RingtoneCreatorCustomAudioWidgetBinding.inflate(layoutInflater, parent, false)
            return CustomAudioViewHolder(adapter, widgetBinding, itemBinding)
        }
    }
}
