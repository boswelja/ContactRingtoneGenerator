package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.BaseItem

abstract class BaseViewHolder(val binding: RingtoneCreatorItemBinding) : RecyclerView.ViewHolder(binding.root) {

    lateinit var widgetView: View

    abstract fun createWidgetView(): View
    open  fun bind(item: BaseItem) {
        widgetView = createWidgetView()
        binding.widgetContainer.addView(widgetView)
    }
}