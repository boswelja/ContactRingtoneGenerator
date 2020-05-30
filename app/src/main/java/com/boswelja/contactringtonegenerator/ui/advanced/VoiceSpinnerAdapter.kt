package com.boswelja.contactringtonegenerator.ui.advanced

import android.content.Context
import android.database.DataSetObserver
import android.speech.tts.Voice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.AppCompatTextView
import com.boswelja.contactringtonegenerator.R

class VoiceSpinnerAdapter(context: Context, private val voices: List<Voice>) : SpinnerAdapter {

    private val layoutInflater = LayoutInflater.from(context)

    private val qualityInfoString = context.getString(R.string.voice_quality_info)
    private val latencyInfoString = context.getString(R.string.voice_latency_info)
    private val veryHighString = context.getString(R.string.very_high)
    private val highString = context.getString(R.string.high)
    private val averageString = context.getString(R.string.average)
    private val lowString = context.getString(R.string.low)
    private val veryLowString = context.getString(R.string.very_low)

    private val voiceNameDefault = context.getString(R.string.voice_name_default)
    private val voiceNameMale = context.getString(R.string.voice_name_male)
    private val voiceNameFemale = context.getString(R.string.voice_name_female)

    override fun getCount(): Int = voices.count()
    override fun isEmpty(): Boolean = voices.isEmpty()
    override fun hasStableIds(): Boolean = false

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItem(position: Int): Any = voices[position]

    override fun getItemViewType(position: Int): Int = 0
    override fun getViewTypeCount(): Int = 1

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val voice = voices[position]
        val view = convertView ?: layoutInflater.inflate(R.layout.voice_spinner_dropdown_view, parent, false)
        view.findViewById<AppCompatTextView>(R.id.voice_name).text = getVoiceName(voice)

        val qualityString = when (voice.quality) {
            Voice.QUALITY_VERY_HIGH -> veryHighString
            Voice.QUALITY_HIGH -> highString
            Voice.QUALITY_NORMAL -> averageString
            Voice.QUALITY_LOW -> lowString
            else -> veryLowString
        }
        view.findViewById<AppCompatTextView>(R.id.voice_quality).text = String.format(qualityInfoString, qualityString)

        val latencyString = when (voice.latency) {
            Voice.LATENCY_VERY_HIGH -> veryHighString
            Voice.LATENCY_HIGH -> highString
            Voice.LATENCY_NORMAL -> averageString
            Voice.LATENCY_LOW -> lowString
            else -> veryLowString
        }
        view.findViewById<AppCompatTextView>(R.id.voice_latency).text = String.format(latencyInfoString, latencyString)
        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val voice = voices[position]
        val view = convertView ?: layoutInflater.inflate(R.layout.voice_spinner_item_view, parent, false)
        view.findViewById<AppCompatTextView>(R.id.voice_name).text = getVoiceName(voice)
        return view
    }

    private fun getVoiceName(voice: Voice): String {
        val name = voice.name
        return when {
            name.endsWith("language") -> {
                voiceNameDefault
            }
            name.contains("female") -> {
                var result = voiceNameFemale
                for (c in name) {
                    if (c.isDigit()) {
                        result += " $c"
                    }
                }
                result
            }
            name.contains("male") -> {
                var result = voiceNameMale
                for (c in name) {
                    if (c.isDigit()) {
                        result += " $c"
                    }
                }
                result
            }
            else -> {
                name
            }
        }
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {}

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {}
}