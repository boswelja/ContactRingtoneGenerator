package com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

class NonDynamicViewHolder private constructor(binding: RingtoneCreatorItemBinding) : BaseViewHolder(binding) {

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

    companion object {
        fun from(parent: ViewGroup): NonDynamicViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = RingtoneCreatorItemBinding.inflate(layoutInflater!!, parent, false)
            return NonDynamicViewHolder(itemBinding)
        }
    }
}
