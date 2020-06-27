package com.boswelja.contactringtonegenerator.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boswelja.contactringtonegenerator.databinding.CommonFragmentListBinding

abstract class ListFragment<T> : BaseDataFragment<T>() {

    protected lateinit var binding: CommonFragmentListBinding

    open fun onCreateWidgetView(): View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CommonFragmentListBinding.inflate(inflater, container, false)
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
