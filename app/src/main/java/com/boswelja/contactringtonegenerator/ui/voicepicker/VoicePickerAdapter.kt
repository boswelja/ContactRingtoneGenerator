package com.boswelja.contactringtonegenerator.ui.voicepicker

import android.content.Context
import android.speech.tts.Voice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.VoiceItemViewBinding
import com.boswelja.contactringtonegenerator.ui.common.SectionedAdapter
import com.google.android.material.radiobutton.MaterialRadioButton

class VoicePickerAdapter(
        context: Context,
        items: ArrayList<Pair<String, ArrayList<Voice>>>,
        private val listener: VoiceSelectedCallback) :
        SectionedAdapter<Voice>(items = items, itemSortMode = SortMode.ASCENDING) {

    private val qualityInfoString = context.getString(R.string.voice_quality_info)
    private val latencyInfoString = context.getString(R.string.voice_latency_info)
    private val veryHighString = context.getString(R.string.very_high)
    private val highString = context.getString(R.string.high)
    private val averageString = context.getString(R.string.average)
    private val lowString = context.getString(R.string.low)
    private val veryLowString = context.getString(R.string.very_low)

    private var selectedItem: Voice = items[0].second[0]
    private var selectedItemPosition: Int = 0

    override fun onCreateItemViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder {
        val binding = VoiceItemViewBinding.inflate(layoutInflater, parent, false)
        return VoiceViewHolder(binding)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, item: Voice, position: Int) {
        if (holder is VoiceViewHolder) {
            val qualityString = when (item.quality) {
                Voice.QUALITY_VERY_HIGH -> veryHighString
                Voice.QUALITY_HIGH -> highString
                Voice.QUALITY_NORMAL -> averageString
                Voice.QUALITY_LOW -> lowString
                else -> veryLowString
            }

            val latencyString = when (item.latency) {
                Voice.LATENCY_VERY_HIGH -> veryHighString
                Voice.LATENCY_HIGH -> highString
                Voice.LATENCY_NORMAL -> averageString
                Voice.LATENCY_LOW -> lowString
                else -> veryLowString
            }

            holder.apply {
                selectedButton.isChecked = position == selectedItemPosition
                itemView.setOnClickListener {
                    setSelectedItem(item, position)
                    selectedButton.isChecked = true
                }
                voiceNameView.text = item.name
                voiceQualityView.text = String.format(qualityInfoString, qualityString)
                voiceLatencyView.text = String.format(latencyInfoString, latencyString)
                previewButton.setOnClickListener {
                    listener.onPreview(item)
                }
            }
        }
    }

    private fun setSelectedItem(item: Voice, position: Int) {
        notifyItemChanged(selectedItemPosition) // Update selected state of the old item
        selectedItem = item
        selectedItemPosition = position
        listener.onSelected(item)
    }

    class VoiceViewHolder(binding: VoiceItemViewBinding) :
            RecyclerView.ViewHolder(binding.root) {

        val voiceNameView: AppCompatTextView = binding.voiceNameView
        val voiceQualityView: AppCompatTextView = binding.voiceQualityView
        val voiceLatencyView: AppCompatTextView = binding.voiceLatencyView
        val selectedButton: MaterialRadioButton = binding.selectedButton
        val previewButton: AppCompatImageButton = binding.previewButton
    }
}