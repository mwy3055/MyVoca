package hsk.practice.myvoca.ui.seeall.recyclerview

import android.os.*
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.View.OnCreateContextMenuListener
import android.view.View.OnLongClickListener
import androidx.core.util.keyIterator
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.SingletonHolder
import hsk.practice.myvoca.databinding.VocaViewBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.ui.seeall.OnDeleteModeListener
import hsk.practice.myvoca.ui.seeall.OnVocabularyUpdateListener
import hsk.practice.myvoca.ui.seeall.SeeAllViewModel
import hsk.practice.myvoca.ui.seeall.ShowVocaOnNotification
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.VocaViewHolder
import java.util.*

/**
 * RecyclerAdapter for each vocabulary.
 * Implemented as Singleton to manage the view more easily.
 *
 * For further information, Please refer the comments above some methods.
 */
class VocaRecyclerViewAdapter private constructor(val viewModel: SeeAllViewModel)
    : RecyclerView.Adapter<VocaViewHolder?>(), OnDeleteModeListener {

    // Custom listener interfaces. Will be used in the ViewHolder below.
    interface OnVocaClickListener {
        fun onVocaClick(holder: VocaViewHolder?, view: View?, position: Int)
        fun onVocaLongClick(holder: VocaViewHolder?, view: View?, position: Int): Boolean
    }

    interface OnSelectModeListener {
        fun onDeleteModeEnabled()
        fun onDeleteModeDisabled()
    }

    companion object : SingletonHolder<VocaRecyclerViewAdapter, SeeAllViewModel>({ viewModel ->
        VocaRecyclerViewAdapter(viewModel)
    })

    val currentVocabulary: LiveData<MutableList<RoomVocabulary?>?>
        get() = viewModel.currentVocabulary

    val deleteMode: Boolean
        get() = viewModel.deleteMode
    private val searchMode: Boolean
        get() = viewModel.searchMode

    private var vocaClickListener: OnVocaClickListener? = null
    private var showVocaOnNotification: ShowVocaOnNotification? = null
    private var onDeleteModeListener: OnSelectModeListener? = null
    private var onVocabularyUpdateListener: OnVocabularyUpdateListener? = null

    private val selectedItems: SparseBooleanArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocaViewHolder {
        val vocaBinding = VocaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val holder = VocaViewHolder(vocaBinding, this, onVocabularyUpdateListener)
        holder.setVocaClickListener(vocaClickListener)
        return holder
    }

    // Bind the content to the item
    override fun onBindViewHolder(holder: VocaViewHolder, position: Int) {
        val vocabulary = currentVocabulary.value?.get(position)
                ?: RoomVocabulary.nullVocabulary
        holder.bind(vocabulary, position)
    }

    // Methods for general adapter
    override fun getItemCount() = currentVocabulary.value?.size ?: 0

    fun getItem(position: Int) = currentVocabulary.value?.get(position)

    override fun getItemId(position: Int) = position.toLong()

    fun getItemPosition(vocabulary: RoomVocabulary?) = currentVocabulary.value?.indexOf(vocabulary)

    // Getter/Setters of Listeners
    fun getVocaClickListener() = vocaClickListener

    fun setOnDeleteModeListener(listener: OnSelectModeListener?) {
        onDeleteModeListener = listener
    }

    fun setVocaClickListener(vocaClickListener: OnVocaClickListener?) {
        this.vocaClickListener = vocaClickListener
    }

    fun setOnEditVocabularyListener(updateListener: OnVocabularyUpdateListener?) {
        onVocabularyUpdateListener = updateListener
    }

    fun setShowVocaOnNotificationListener(notificationListener: ShowVocaOnNotification?) {
        showVocaOnNotification = notificationListener
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
        for (i in currentVocabulary.value?.indices!!) {
            notifyItemChanged(i)
        }
    }

    /* for search mode */
    fun enableSearchMode() {
//        searchMode = true
        viewModel.enableSearchMode()
    }

    fun disableSearchMode() {
        if (searchMode) {
//            searchMode = false
//            currentVocabulary = viewModel.allVocabulary
//            sortItems(sortState)
            viewModel.disableSearchMode()
            notifyDataSetChanged()
        }
    }

    fun searchVocabulary(query: String) {
//        currentVocabulary = viewModel.getVocabulary("%$query%")
//        currentVocabulary?.observeForever {
//            Log.d(AppHelper.LOG_TAG, "Searched $query: ${it?.size ?: -1}")
//            sortItems(sortState)
//            notifyDataSetChanged()
//        }
        viewModel.searchVocabulary("%$query%")
        notifyDataSetChanged()
    }

    /**
     * Swipe to remove one item.
     * @param position position whose item to remove
     */
    fun removeItem(position: Int) {
//        val deletedVocabulary = currentVocabulary?.value?.get(position)
//        currentVocabulary?.value?.removeAt(position)
        viewModel.deleteItem(position)
        notifyItemRemoved(position)
//        if (deletedVocabulary != null) {
//            viewModel.deleteVocabulary(deletedVocabulary)
//        }
    }

    fun deleteVocabularies() {
        val selectedItems = getSelectedItems()
        viewModel.deleteItems(selectedItems)
//        val selected = getSelectedItems()
//        // reverse iteration
//        val iterator: MutableListIterator<*> = selected.listIterator(selected.size)
//        while (iterator.hasPrevious()) {
//            val vocabulary = getItem(iterator.previous() as Int)
//            if (vocabulary != null) {
//                viewModel.deleteVocabulary(vocabulary)
//            }
//        }
        disableDeleteMode()
    }

    fun restoreItem(vocabulary: RoomVocabulary, position: Int) {
//        currentVocabulary?.value?.add(position, vocabulary)
        viewModel.restoreItem(vocabulary, position)
        notifyItemInserted(position)
//        if (vocabulary != null) {
//            viewModel.insertVocabulary(vocabulary)
//        }
    }

    fun sortItems(method: Int) {
        viewModel.sortItems(method)
        notifyDataSetChanged()
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
     */
    inner class VocaViewHolder(private val vocaBinding: VocaViewBinding,
                               onDeleteModeListener: OnDeleteModeListener?,
                               onVocabularyUpdateListener: OnVocabularyUpdateListener?) : RecyclerView.ViewHolder(vocaBinding.root), OnCreateContextMenuListener {

        private var vocaClickListener: OnVocaClickListener? = null
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
            setVocaClickListener(vocaClickListener)
            if (vocabulary != RoomVocabulary.nullVocabulary) {
                setDeleteCheckBox(position)
            }
//            if (currentVocabulary?.value == null) {
//                setVocabulary(RoomVocabulary.nullVocabulary)
//                setVocaClickListener(vocaClickListener)
//            } else {
//                val vocabulary = currentVocabulary.value?.get(position)
//                setVocabulary(vocabulary)
//                setVocaClickListener(vocaClickListener)
//                vocaBinding.deleteCheckBox.apply {
//                    if (deleteMode) {
//                        visibility = View.VISIBLE
//                        isChecked = selectedItems.get(position)
//                    } else {
//                        visibility = View.GONE
//                        isChecked = false
//                    }
//                }
//            }
        }

        private fun setVocabulary(vocabulary: RoomVocabulary?) {
            vocabulary?.apply {
                vocaBinding.vocaEng.text = eng
                vocaBinding.vocaKor.text = kor
                vocaBinding.lastEditTime.text = AppHelper.getTimeString(lastEditedTime.toLong() * 1000)
            }
        }

        @JvmName("setVocaClickListener1")
        fun setVocaClickListener(vocaClickListener: OnVocaClickListener?) {
            this.vocaClickListener = vocaClickListener
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
                    vocaClickListener!!.onVocaClick(this@VocaViewHolder, v, position)
                }
            }
            vocaBinding.root.setOnLongClickListener(OnLongClickListener { v ->
                if (vocaClickListener != null) {
                    val position = adapterPosition
                    return@OnLongClickListener vocaClickListener!!.onVocaLongClick(this@VocaViewHolder, v, position)
                }
                false
            })
            this.onDeleteModeListener = onDeleteModeListener
            this.onVocabularyUpdateListener = onVocabularyUpdateListener
            vocaBinding.root.setOnCreateContextMenuListener(this)
        }
    }
}