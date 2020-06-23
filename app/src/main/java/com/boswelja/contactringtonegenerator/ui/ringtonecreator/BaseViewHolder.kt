package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.BaseItem

abstract class BaseViewHolder(val binding: RingtoneCreatorItemBinding) : RecyclerView.ViewHolder(binding.root) {

    lateinit var widgetView: View

    fun initWidgetView() {
        widgetView = createWidgetView()
        binding.widgetContainer.addView(widgetView)
    }

    abstract fun createWidgetView(): View
    abstract fun bind(item: BaseItem)
}
