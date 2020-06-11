package com.boswelja.contactringtonegenerator.ui.enginepicker

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.ui.MainActivity
import com.boswelja.contactringtonegenerator.ui.common.FragmentEasyModeList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EnginePickerFragment : FragmentEasyModeList<String>() {

    private val adapter = EngineAdapter()
    private val coroutineScope = MainScope()

    override fun onSaveData(activity: MainActivity, data: String) {
        activity.ttsEngine = data
    }

    override fun requestData(): String? {
        return adapter.selectedEngine
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@EnginePickerFragment.adapter
        }
        updateAvailableEngines()
    }

    private fun updateAvailableEngines() {
        setLoading(true)
        coroutineScope.launch(Dispatchers.Default) {
            val mainActivity = activity
            val engines = if (mainActivity is MainActivity) {
                mainActivity.ttsManager.engines
            } else {
                emptyList()
            }
            withContext(Dispatchers.Main) {
                adapter.setEngines(engines)
                setLoading(false)
            }
        }
    }
}