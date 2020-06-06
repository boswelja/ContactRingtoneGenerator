package com.boswelja.contactringtonegenerator.ui.enginepicker

import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.databinding.TtsEngineItemViewBinding

class EngineAdapter : RecyclerView.Adapter<EngineAdapter.ViewHolder>() {

    private val engines = ArrayList<TextToSpeech.EngineInfo>()

    private var layoutInflater: LayoutInflater? = null
    var selectedEngine: String? = if (engines.isNotEmpty()) engines[0].name else null
        private set

    override fun getItemCount(): Int = engines.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        val binding = TtsEngineItemViewBinding.inflate(layoutInflater!!, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val engine = engines[position]
        holder.bind(engine)
        holder.binding.engineDescriptionView.apply {
            if (engine.name == selectedEngine) {
                text = "Default"
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
        holder.binding.selectedButton.isChecked = engine.name == selectedEngine
        holder.itemView.setOnClickListener {
            val lastSelectedEngine = selectedEngine
            selectedEngine = engine.name
            holder.binding.selectedButton.isChecked = true
            notifyItemChanged(engines.indexOfFirst { it.name == lastSelectedEngine })
        }
    }

    fun setEngines(newEngines: List<TextToSpeech.EngineInfo>) {
        engines.apply {
            clear()
            addAll(newEngines)
            selectedEngine = if (isNotEmpty()) get(0).name else null
        }
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: TtsEngineItemViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind (info: TextToSpeech.EngineInfo) {
            binding.apply {
                engineIconView.setImageDrawable(
                        itemView.context.packageManager.getApplicationIcon(info.name))
                engineNameView.text = info.label
            }
        }
    }
}