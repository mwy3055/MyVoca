package hsk.practice.myvoca.ui.seeall.recyclerview

import android.graphics.Color
import android.os.*
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.View.OnCreateContextMenuListener
import android.view.View.OnLongClickListener
import androidx.core.util.keyIterator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.databinding.VocaViewBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.ui.seeall.SeeAllViewModel
import hsk.practice.myvoca.ui.seeall.listeners.OnDeleteModeListener
import hsk.practice.myvoca.ui.seeall.listeners.OnVocabularyUpdateListener
import hsk.practice.myvoca.ui.seeall.listeners.ShowVocaOnNotification
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.VocaViewHolder
import java.util.*

/**
 * RecyclerAdapter for each vocabulary.
 * Implemented as Singleton to manage the view more easily.
 *
 * For further information, Please refer the comments above some methods.
 */
class VocaRecyclerViewAdapter(val viewModel: SeeAllViewModel,
                              val vocaClickListener: OnVocaClickListener? = null,
                              val showVocaOnNotification: ShowVocaOnNotification? = null,
                              val onDeleteModeListener: OnSelectModeListener? = null,
                              val onVocabularyUpdateListener: OnVocabularyUpdateListener? = null)
    : ListAdapter<RoomVocabulary, VocaViewHolder>(RoomVocabularyDiffCallback()), OnDeleteModeListener {

    // Custom listener interfaces. Will be used in the ViewHolder below.
    interface OnVocaClickListener {
        fun onVocaClick(holder: VocaViewHolder?, view: View?, position: Int)
        fun onVocaLongClick(holder: VocaViewHolder?, view: View?, position: Int): Boolean
    }

    interface OnSelectModeListener {
        fun onDeleteModeEnabled()
        fun onDeleteModeDisabled()
    }

    val deleteMode: Boolean
        get() = viewModel.deleteMode
    private val searchMode: Boolean
        get() = viewModel.searchMode

    private val selectedItems: SparseBooleanArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocaViewHolder {
        val vocaBinding = VocaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // TODO: replace to companion object method VocaViewHolder.from(...)
        val holder = VocaViewHolder(vocaBinding, this, onVocabularyUpdateListener, vocaClickListener)
        return holder
    }

    // Bind the content to the item
    override fun onBindViewHolder(holder: VocaViewHolder, position: Int) {
        val vocabulary = getItem(position) ?: RoomVocabulary.nullVocabulary
        holder.bind(vocabulary, position)
    }

    // Methods for managing select state
    fun isSelected(position: Int) = getSelectedItems().contains(position)

    fun switchSelectedState(position: Int) {
        if (selectedItems.get(position)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

    fun clearSelectedState() {
        selectedItems.clear()
        notifyItemsChanged()
    }

    fun getSelectedItemCount() = selectedItems.size()

    fun getSelectedItems(): List<Int> {
        val items = mutableListOf<Int>()
        for (item in selectedItems.keyIterator()) {
            items.add(item)
        }
        return items
    }

    fun notifyItemsChanged() {
        for (i in 0 until itemCount) {
            notifyItemChanged(i)
        }
    }

    /* for search mode */
    fun enableSearchMode() {
        viewModel.enableSearchMode()
    }

    fun disableSearchMode() {
        if (searchMode) {
            viewModel.disableSearchMode()
        }
    }

    fun searchVocabulary(query: String) {
        viewModel.searchVocabulary("%$query%")
    }

    /**
     * Swipe to remove one item.
     * @param position position whose item to remove
     */
    fun removeItem(position: Int) {
        viewModel.deleteItem(position)
    }

    fun deleteVocabularies() {
        val selectedItems = getSelectedItems()
        viewModel.deleteItems(selectedItems)
        disableDeleteMode()
    }

    fun restoreItem(vocabulary: RoomVocabulary, position: Int) {
        viewModel.restoreItem(vocabulary, position)
    }

    fun sortItems(method: Int) {
        viewModel.sortItems(method)
    }

    fun showDeleteSnackbar(view: View, position: Int) {
        val deletedVocabulary = getItem(position)
        val eng = deletedVocabulary.eng
        Log.d("HSK APP", "pos: $position")
        removeItem(position)
        val snackBar = Snackbar.make(view, eng + "이(가) 삭제되었습니다.", Snackbar.LENGTH_LONG)
        snackBar.setAction("실행 취소") {
            restoreItem(deletedVocabulary, position)
        }
        snackBar.setActionTextColor(Color.YELLOW)
        snackBar.show()
    }

    // See SeeAllFragment.onDeleteModeEnabled() Method
    override fun enableDeleteMode() {
        Log.d("HSK APP", "DELETE MODE ENABLED")
        viewModel.deleteMode = true
        notifyItemsChanged()
        onDeleteModeListener?.onDeleteModeEnabled()
    }

    // See SeeAllFragment.onDeleteModeDisabled() Method
    override fun disableDeleteMode() {
        Log.d("HSK APP", "DELETE MODE DISABLED")
        viewModel.deleteMode = false
        notifyItemsChanged()
        onDeleteModeListener?.onDeleteModeDisabled()
    }

    /**
     * ViewHolder for vocabulary object.
     * Manages the content of the item and action when the item is clicked or long-clicked.
     *
     * TODO: convert to pure class, not a inner class
     */
    inner class VocaViewHolder(private val vocaBinding: VocaViewBinding,
                                                   onDeleteModeListener: OnDeleteModeListener?,
                                                   onVocabularyUpdateListener: OnVocabularyUpdateListener?,
                                                   vocaClickListener: OnVocaClickListener? = null) : RecyclerView.ViewHolder(vocaBinding.root), OnCreateContextMenuListener {

        private var onDeleteModeListener: OnDeleteModeListener?
        private var onVocabularyUpdateListener: OnVocabularyUpdateListener?
        var viewForeground = vocaBinding.viewForeground
        var viewBackground = vocaBinding.viewBackground

        // long-click listener
        private val onMenuItemClickListener: MenuItem.OnMenuItemClickListener = MenuItem.OnMenuItemClickListener { item ->
            val position = adapterPosition
            val vocabulary = viewModel.currentVocabulary.value?.get(position)
                    ?: return@OnMenuItemClickListener true
            Log.d("HSK APP", position.toString())
            when (item.itemId) {
                Constants.EDIT_CODE -> {
                    onVocabularyUpdateListener?.updateVocabulary(position)
                }
                Constants.DELETE_CODE -> {
                    val adapter = this@VocaRecyclerViewAdapter
                    adapter.enableDeleteMode()
                    adapter.switchSelectedState(position)
                }
                Constants.SHOW_ON_NOTIFICATION_CODE -> {
                    // TODO: show selected vocabulary on notification
                    showVocaOnNotification?.showVocabularyOnNotification(vocabulary)
                }
            }
            true
        }

        // Create drop-down menu when item is long-clicked
        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
            val adapter = this@VocaRecyclerViewAdapter
            if (adapter.deleteMode) {
                return
            }
            val edit = menu?.add(Menu.NONE, Constants.EDIT_CODE, 1, "수정")
            val delete = menu?.add(Menu.NONE, Constants.DELETE_CODE, 2, "삭제")
            //            MenuItem showOnNotification = menu.add(Menu.NONE, Constants.SHOW_ON_NOTIFICATION_CODE, 3, "알림에 보이기");
            edit?.setOnMenuItemClickListener(onMenuItemClickListener)
            delete?.setOnMenuItemClickListener(onMenuItemClickListener)
            //            showOnNotification.setOnMenuItemClickListener(onMenuItemClickListener);
        }

        fun bind(vocabulary: RoomVocabulary, position: Int) {
            setVocabulary(vocabulary)
//            this@VocaRecyclerViewAdapter.vocaClickListener = vocaClickListener
            if (vocabulary != RoomVocabulary.nullVocabulary) {
                setDeleteCheckBox(position)
            }
        }

        private fun setVocabulary(vocabulary: RoomVocabulary?) {
            vocabulary?.apply {
                vocaBinding.vocaEng.text = eng
                vocaBinding.vocaKor.text = kor
                vocaBinding.lastEditTime.text = AppHelper.getTimeString(lastEditedTime.toLong() * 1000)
            }
        }

        private fun setDeleteCheckBox(position: Int) {
            vocaBinding.deleteCheckBox.apply {
                if (deleteMode) {
                    visibility = View.VISIBLE
                    isChecked = selectedItems.get(position)
                } else {
                    visibility = View.GONE
                    isChecked = false
                }
            }
        }

        init {
            vocaBinding.root.setOnClickListener { v ->
                if (vocaClickListener != null) {
                    val position = adapterPosition
                    vocaClickListener.onVocaClick(this@VocaViewHolder, v, position)
                }
            }
            vocaBinding.root.setOnLongClickListener(OnLongClickListener { v ->
                if (vocaClickListener != null) {
                    val position = adapterPosition
                    return@OnLongClickListener vocaClickListener.onVocaLongClick(this@VocaViewHolder, v, position)
                }
                false
            })
            this.onDeleteModeListener = onDeleteModeListener
            this.onVocabularyUpdateListener = onVocabularyUpdateListener
            vocaBinding.root.setOnCreateContextMenuListener(this)
        }
    }
}

class RoomVocabularyDiffCallback : DiffUtil.ItemCallback<RoomVocabulary>() {
    override fun areContentsTheSame(oldItem: RoomVocabulary, newItem: RoomVocabulary): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: RoomVocabulary, newItem: RoomVocabulary): Boolean {
        return oldItem.eng == newItem.eng
    }
}