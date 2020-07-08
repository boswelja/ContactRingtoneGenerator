package com.boswelja.contactringtonegenerator.ui.ringtonecreator.holder

import android.view.View
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorCustomAudioWidgetBinding
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.RingtoneCreatorAdapter

class CustomAudioViewHolder(
        private val adapter: RingtoneCreatorAdapter,
        private val widgetBinding: RingtoneCreatorCustomAudioWidgetBinding,
        binding: RingtoneCreatorItemBinding
) : BaseViewHolder(binding) {

    private val noFileText: String = itemView.context.getString(R.string.item_audio_no_file)

    init {
        initWidgetView()
    }

    override fun createWidgetView(): View {
        return widgetBinding.root
    }

    override fun bind(item: StructureItem) {
        if (item is AudioItem) {
            val uri = item.getAudioContentUri()
            val filePathText = uri?.lastPathSegment ?: noFileText
            widgetBinding.apply {
                fileNameView.text = filePathText
                browseButton.setOnClickListener {
                    adapter.startChooserForResult(adapterPosition)
                }
            }
            adapter.setIsDataValid(adapterPosition, uri != null)
        }
    }
}
