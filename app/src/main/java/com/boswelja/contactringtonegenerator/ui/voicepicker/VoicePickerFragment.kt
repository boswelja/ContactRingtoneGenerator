package com.boswelja.contactringtonegenerator.ui.voicepicker

import android.os.Bundle
import android.speech.tts.Voice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.FragmentEasyModeListBinding
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.boswelja.contactringtonegenerator.ui.MainActivity
import com.boswelja.contactringtonegenerator.ui.common.SectionedAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class VoicePickerFragment : Fragment(), VoiceSelectedCallback {

    private val coroutineScope = MainScope()

    private var selectedVoice: Voice? = null

    private lateinit var binding: FragmentEasyModeListBinding

    override fun onPreview(item: Voice) {
        (activity as MainActivity).ttsManager.previewVoice(item, "This is what this voice sounds like")
    }

    override fun onSelected(item: Voice) {
        selectedVoice = item
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEasyModeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLoading(true)
        binding.apply {
            recyclerView.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            nextButton.setOnClickListener {
                findNavController().navigate(VoicePickerFragmentDirections.toRingtoneCreatorFragment())
            }
            titleView.setText(R.string.voice_picker_title)
            subtitleView.visibility = View.GONE
        }
        updateVoices((activity as MainActivity).ttsManager)
    }

    private fun setLoading(loading: Boolean) {
        binding.apply {
            if (loading) {
                loadingSpinner.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            } else {
                loadingSpinner.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun updateVoices(tts: TtsManager) {
        coroutineScope.launch(Dispatchers.IO) {
            val voices = tts.getAvailableVoices(Locale.getDefault())
            if (voices != null) {
                val defaultSection = Pair<String, ArrayList<Voice>>(SectionedAdapter.SECTION_HEADER_HIDDEN, ArrayList())
                val defaultVoice = tts.getDefaultVoice()
                        ?: throw IllegalStateException("TTS engine not initialized")
                defaultSection.second.add(defaultVoice)
                val maleSection = Pair<String, ArrayList<Voice>>(getString(R.string.voice_name_male), ArrayList())
                val femaleSection = Pair<String, ArrayList<Voice>>(getString(R.string.voice_name_female), ArrayList())
                val undefinedSection = Pair<String, ArrayList<Voice>>("Undefined", ArrayList())
                voices.forEach {
                    val name = it.name
                    when {
                        name.contains("female") -> {
                            femaleSection.second.add(it)
                        }
                        name.contains("male") -> {
                            maleSection.second.add(it)
                        }
                        else -> {
                            undefinedSection.second.add(it)
                        }
                    }
                }
                val result = ArrayList<Pair<String, ArrayList<Voice>>>()
                result.add(defaultSection)
                if (maleSection.second.isNotEmpty()) result.add(maleSection)
                if (femaleSection.second.isNotEmpty()) result.add(femaleSection)
                //result.add(undefinedSection)
                withContext(Dispatchers.Main) {
                    binding.recyclerView.adapter =
                            VoicePickerAdapter(requireContext(), result, this@VoicePickerFragment)
                    setLoading(false)
                }
            } else {
                // Voices list null, engine likely not initialized
//                withContext(Dispatchers.Main) {
//                    findNavController().navigate(VoicePickerFragmentDirections.toRingtoneCreatorFragment())
//                }
            }
        }
    }
}
