package com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.holder

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomText
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.RingtoneCreatorAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CustomTextViewHolder(
        private val adapter: RingtoneCreatorAdapter,
        binding: RingtoneCreatorItemBinding
) :
    BaseViewHolder(binding) {

    init {
        initWidgetView()
    }

    override fun createWidgetView(): View {
        return TextInputLayout(itemView.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            TextInputEditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                doAfterTextChanged {
                    val item = adapter.getItem(adapterPosition)
                    if (item is CustomText) {
                        item.text = it.toString()
                        adapter.setIsDataValid(adapterPosition, item.text.isNotBlank())
                    }
                }
            }.also {
                addView(it)
            }
        }
    }

    override fun bind(item: StructureItem) {
        if (item is CustomText) {
            (widgetView as TextInputLayout).editText?.apply {
                setText(item.text)
                setHint(item.getLabelRes())
            }
        }
    }

    companion object {
        fun from(adapter: RingtoneCreatorAdapter, parent: ViewGroup): CustomTextViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = RingtoneCreatorItemBinding.inflate(layoutInflater!!, parent, false)
            return CustomTextViewHolder(adapter, itemBinding)
        }
    }
}
