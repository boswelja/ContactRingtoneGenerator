package com.boswelja.contactringtonegenerator.ui

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

    override fun getCount(): Int = voices.count()
    override fun isEmpty(): Boolean = voices.isEmpty()
    override fun hasStableIds(): Boolean = false

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItem(position: Int): Any = voices[position]

    override fun getItemViewType(position: Int): Int = 0
    override fun getViewTypeCount(): Int = 1

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val voice = voices[position]
        val view = convertView ?: layoutInflater.inflate(R.layout.voice_dropdown_view, parent, false)
        view.findViewById<AppCompatTextView>(R.id.voice_name).text = getVoiceName(voice)
        view.findViewById<AppCompatTextView>(R.id.voice_quality).text = "Quality: " + when (voice.quality) {
            Voice.QUALITY_VERY_HIGH -> "Very High"
            Voice.QUALITY_HIGH -> "High"
            Voice.QUALITY_NORMAL -> "Average"
            Voice.QUALITY_LOW -> "Low"
            else -> "Very Low"
        }
        view.findViewById<AppCompatTextView>(R.id.voice_latency).text = "Latency: " + when (voice.quality) {
            Voice.LATENCY_VERY_HIGH -> "Very High"
            Voice.LATENCY_HIGH -> "High"
            Voice.LATENCY_NORMAL -> "Average"
            Voice.LATENCY_LOW -> "Low"
            else -> "Very Low"
        }
        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val voice = voices[position]
        val view = convertView ?: layoutInflater.inflate(R.layout.voice_view, parent, false)
        view.findViewById<AppCompatTextView>(R.id.voice_name).text = getVoiceName(voice)
        return view
    }

    private fun getVoiceName(voice: Voice): String {
        val name = voice.name
        return if (name.endsWith("language")) {
            "Default"
        } else if (name.contains("female")) {
            var result = "Female"
            for (c in name) {
                if (c.isDigit()) {
                    result += " $c"
                }
            }
            result
        } else if (name.contains("male")) {
            var result = "Male"
            for (c in name) {
                if (c.isDigit()) {
                    result += " $c"
                }
            }
            result
        } else {
            name
        }

    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {}

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {}
}