package com.boswelja.contactringtonegenerator.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boswelja.contactringtonegenerator.databinding.FragmentEasyModeListBinding

abstract class FragmentEasyModeList : Fragment() {

    protected lateinit var binding: FragmentEasyModeListBinding

    open fun onCreateWidgetView(): View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEasyModeListBinding.inflate(inflater, container, false)
        val widgetView = onCreateWidgetView()
        if (widgetView != null) binding.widgetContainer.addView(widgetView, 0)
        return binding.root
    }

    protected fun setLoading(loading: Boolean) {
        binding.apply {
            if (loading) {
                loadingSpinner.visibility = View.VISIBLE
                recyclerView.isEnabled = false
            } else {
                loadingSpinner.visibility = View.INVISIBLE
                recyclerView.isEnabled = true
            }
        }
    }

}