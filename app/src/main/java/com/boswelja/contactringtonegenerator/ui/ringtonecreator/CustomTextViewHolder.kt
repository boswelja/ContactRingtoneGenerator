package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.text.InputType
import android.view.View
import android.widget.LinearLayout
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.BaseItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CustomTextViewHolder(binding: RingtoneCreatorItemBinding) : BaseViewHolder(binding) {

    override fun createWidgetView(): View {
        return TextInputLayout(itemView.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            hint = "Custom Text"
            endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            TextInputEditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }.also {
                addView(it)
            }
        }
    }

    override fun bind(item: BaseItem) {
        super.bind(item)
    }
}