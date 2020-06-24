package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.graphics.Canvas
import android.view.HapticFeedbackConstants
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.R

class AdapterGestureHelper(val adapter: RingtoneCreatorAdapter) :
    ItemTouchHelper.SimpleCallback(UP or DOWN, START or END) {

    private var isElevated: Boolean = false

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        adapter.moveItem(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.removeItem(position)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (viewHolder != null) {
            when (actionState) {
                ACTION_STATE_DRAG -> {
                    viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // To avoid elevation conflicts with the system implementation, we will always inform the super that we aren't active
        when (actionState) {
            ACTION_STATE_DRAG -> {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false)
                if (isCurrentlyActive && !isElevated) {
                    updateElevation(viewHolder, true)
                }
            }
            ACTION_STATE_SWIPE -> {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (isElevated) {
            updateElevation(viewHolder, false)
        }
    }

    private fun updateElevation(viewHolder: RecyclerView.ViewHolder, elevate: Boolean) {
        if (elevate) {
            isElevated = true
            ViewCompat.setElevation(
                viewHolder.itemView,
                viewHolder.itemView.resources.getDimension(R.dimen.adapter_gesture_elevation)
            )
        } else {
            isElevated = false
            ViewCompat.setElevation(viewHolder.itemView, 0f)
        }
    }

    companion object {
        fun attachToRecyclerView(recyclerView: RecyclerView) {
            ItemTouchHelper(AdapterGestureHelper(recyclerView.adapter as RingtoneCreatorAdapter)).attachToRecyclerView(recyclerView)
        }
    }
}
