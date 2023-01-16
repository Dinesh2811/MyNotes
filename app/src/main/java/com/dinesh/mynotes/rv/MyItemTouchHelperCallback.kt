package com.dinesh.mynotes.rv

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.dinesh.mynotes.room.Note
import java.util.*

class MyItemTouchHelperCallback(private val rvAdapter: RvAdapter, private val rvMain: RvMain) : ItemTouchHelper.Callback() {

    private var isDragEnabled = true
    fun setDragEnable(dragEnabled: Boolean) {
        isDragEnabled = dragEnabled
    }

    override fun isLongPressDragEnabled(): Boolean {
        return isDragEnabled && rvAdapter.getSelectedItems().size < 2
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT, 0)
//            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.START or ItemTouchHelper.END, 0)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        // When an item is selected
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.alpha = 0.5f
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        rvMain.onItemMove(fromPosition, toPosition)
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        // When an item is dropped
        viewHolder.itemView.alpha = 1.0f
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }


    }






/*



class MyItemTouchHelperCallback(private val rvAdapter: RvAdapter, private val rvMain: RvMain) : ItemTouchHelper.Callback() {

        private var isDragEnabled = true
        fun setDragEnable(dragEnabled: Boolean) {
            isDragEnabled = dragEnabled
        }

    override fun isLongPressDragEnabled(): Boolean {
        return isDragEnabled && rvAdapter.getSelectedItems().size < 2
    }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT, 0)
//            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.START or ItemTouchHelper.END, 0)
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            // When an item is selected
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder?.itemView?.alpha = 0.5f
            }
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            rvMain.onItemMove(fromPosition, toPosition)
            return true
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            // When an item is dropped
            viewHolder.itemView.alpha = 1.0f
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }


    }




 */