package com.boswelja.contactringtonegenerator.ui.ringtonecreator.holder

import android.content.Intent
import android.view.View
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorCustomAudioWidgetBinding
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

class CustomAudioViewHolder(
        private val widgetBinding: RingtoneCreatorCustomAudioWidgetBinding,
        binding: RingtoneCreatorItemBinding
) : BaseViewHolder(binding) {

    private val noFileText: String = itemView.context.getString(R.string.item_audio_no_file)
    private val chooserIntent = Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "audio/*"
    }, "Pick an audio file")

    init {
        initWidgetView()
    }

    override fun createWidgetView(): View {
        return widgetBinding.root
    }

    override fun bind(item: StructureItem) {
        if (item is AudioItem) {
            val filePathText = item.getAudioContentUri()?.toString() ?: noFileText
            widgetBinding.apply {
                fileNameView.text = filePathText
                browseButton.setOnClickListener {
                    itemView.context.startActivity(chooserIntent)
                }
            }
        }
    }
}
