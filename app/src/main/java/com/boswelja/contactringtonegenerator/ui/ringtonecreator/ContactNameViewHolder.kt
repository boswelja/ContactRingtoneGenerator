package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.BaseItem

class ContactNameViewHolder(binding: RingtoneCreatorItemBinding) : BaseViewHolder(binding) {

    override fun createWidgetView(): View {
        return AppCompatTextView(itemView.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1)
        }
    }


    override fun bind(item: BaseItem) {
        super.bind(item)
        (widgetView as AppCompatTextView).text = item.getLabel()
    }
}