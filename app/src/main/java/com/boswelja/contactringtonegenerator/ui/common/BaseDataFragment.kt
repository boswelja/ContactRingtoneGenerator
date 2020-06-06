package com.boswelja.contactringtonegenerator.ui.common

import androidx.fragment.app.Fragment
import com.boswelja.contactringtonegenerator.ui.MainActivity

abstract class BaseDataFragment<T> : Fragment() {

    /**
     * Checks whether the data from [requestData] can be saved, and calls [onSaveData] if possible.
     */
    fun saveData() {
        val data = requestData()
        val mainActivity = activity
        if (data != null && mainActivity is MainActivity) {
            onSaveData(mainActivity, data)
        }
    }

    /**
     * Called when the data is ready to be saved.
     * @param activity The host [MainActivity].
     * @param data The data [T] to save.
     */
    abstract fun onSaveData(activity: MainActivity, data: T)

    /**
     * Requests the data the fragment has stored.
     * @return The stored data [T], or null if it doesn't exist.
     */
    open fun requestData(): T? = null
}