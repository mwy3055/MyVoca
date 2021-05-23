package hsk.practice.myvoca.ui.seeall.recyclerview

import android.os.*
import android.util.SparseBooleanArray
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.View.OnCreateContextMenuListener
import androidx.core.util.keyIterator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hsk.practice.myvoca.databinding.VocaViewBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.getTimeString
import hsk.practice.myvoca.ui.seeall.SeeAllViewModel
import hsk.practice.myvoca.ui.seeall.SortMethod
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.VocaViewHolder
import java.util.*

/**
 * RecyclerAdapter for each vocabulary.
 * Implemented as Singleton to manage the view more easily.
 *
 * For further information, Please refer the comments above some methods.
 */
class VocaRecyclerViewAdapter(val viewModel: SeeAllViewModel) :
    ListAdapter<RoomVocabulary, VocaViewHolder>(RoomVocabularyDiffCallback()) {

    val deleteMode: Boolean
        get() = viewModel.deleteMode.value!!
    private val searchMode: Boolean
        get() = viewModel.searchMode

    private val selectedItems: SparseBooleanArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocaViewHolder {
        val vocaBinding =
            VocaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // TODO: replace to companion object method VocaViewHolder.from(...)
        val holder = VocaViewHolder(vocaBinding)
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
        if (!deleteMode) return
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

    fun searchVocabulary(query: String) {
        viewModel.searchVocabulary("%$query%")
    }

    fun deleteVocabularies() {
        val selectedItems = getSelectedItems()
        viewModel.deleteItems(selectedItems)
    }

    fun sortItems(method: Int) {
        viewModel.setSortState(SortMethod.get(method)!!)
    }

    /**
     * ViewHolder for vocabulary object.
     * Manages the content of the item and action when the item is clicked or long-clicked.
     */
    inner class VocaViewHolder(private val vocaBinding: VocaViewBinding) :
        RecyclerView.ViewHolder(vocaBinding.root), OnCreateContextMenuListener {

        private val EDIT_CODE = 100
        private val DELETE_CODE = 101
        private val SHOW_ON_NOTIFICATION_CODE = 102

        var viewForeground = vocaBinding.viewForeground
        var viewBackground = vocaBinding.viewBackground

        // long-click listener
        private val onMenuItemClickListener: MenuItem.OnMenuItemClickListener =
            MenuItem.OnMenuItemClickListener { item ->
                val position = layoutPosition
                // val position = adapterPosition
                val vocabulary = viewModel.currentVocabulary.value?.get(position)
                    ?: return@OnMenuItemClickListener true
                when (item.itemId) {
                    EDIT_CODE -> {
//                    onVocabularyUpdateListener?.updateVocabulary(position)
                        viewModel.onVocabularyUpdate(position)
                    }
                    DELETE_CODE -> {
                        val adapter = this@VocaRecyclerViewAdapter
                        viewModel.onDeleteModeChange(true)
                        adapter.switchSelectedState(position)
                    }
                    SHOW_ON_NOTIFICATION_CODE -> {
                        // TODO: show selected vocabulary on notification
                        viewModel.onShowVocabulary(vocabulary)
                    }
                }
                true
            }

        // Create drop-down menu when item is long-clicked
        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
            if (viewModel.deleteMode.value == true) {
                return
            }
            val edit = menu?.add(Menu.NONE, EDIT_CODE, 1, "수정")
            val delete = menu?.add(Menu.NONE, DELETE_CODE, 2, "삭제")
            //            MenuItem showOnNotification = menu.add(Menu.NONE, SHOW_ON_NOTIFICATION_CODE, 3, "알림에 보이기");
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
                vocaBinding.lastEditTime.text = (lastEditedTime * 1000).getTimeString()
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
            vocaBinding.root.setOnClickListener {
                switchSelectedState(absoluteAdapterPosition)
            }
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