package com.boswelja.contactringtonegenerator.ui.ringtonecreator.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

class NonDynamicViewHolder(binding: RingtoneCreatorItemBinding) : BaseViewHolder(binding) {

    init {
        initWidgetView()
    }

    override fun createWidgetView(): View {
        return AppCompatTextView(itemView.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1)
        }
    }

    override fun bind(item: StructureItem) {
        (widgetView as AppCompatTextView).setText(item.getLabelRes())
    }
}
