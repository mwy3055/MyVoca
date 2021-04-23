package hsk.practice.myvoca.ui.seeall.recyclerview

import android.graphics.Canvas
import android.view.View
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.VocaViewHolder

/**
 * Touch helper class.
 * Related to swipe actions at the SeeAllFragment.VocaRecyclerView
 */
class VocabularyTouchHelper(dragDirs: Int, swipeDirs: Int, private val deleteMode: LiveData<Boolean>, private val listener: VocabularyTouchHelperListener?)
    : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    interface VocabularyTouchHelperListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (deleteMode.value == false) {
            listener?.onSwiped(viewHolder, direction, viewHolder.absoluteAdapterPosition)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView: View? = (viewHolder as VocaViewHolder?)?.viewForeground
            getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (deleteMode.value == false) {
            val foregroundView: View = (viewHolder as VocaViewHolder).viewForeground
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (deleteMode.value == false) {
            val foregroundView: View? = (viewHolder as VocaViewHolder?)?.viewForeground
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (deleteMode.value == false) {
            val foregroundView: View = (viewHolder as VocaViewHolder).viewForeground
            getDefaultUIUtil().clearView(foregroundView)
        }
    }

}