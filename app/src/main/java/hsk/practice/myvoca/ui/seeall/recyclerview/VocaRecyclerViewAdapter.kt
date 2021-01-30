package hsk.practice.myvoca.ui.seeall.recyclerview

import android.os.*
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.View.OnCreateContextMenuListener
import android.view.View.OnLongClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.databinding.VocaViewBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
import hsk.practice.myvoca.ui.NewVocaViewModel
import hsk.practice.myvoca.ui.NewVocaViewModelFactory
import hsk.practice.myvoca.ui.seeall.OnDeleteModeListener
import hsk.practice.myvoca.ui.seeall.OnVocabularyUpdateListener
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.VocaViewHolder
import java.util.*

/**
 * RecyclerAdapter for each vocabulary.
 * Implemented as Singleton to manage the view more easily.
 *
 * For further information, Please refer the comments above some methods.
 */
class VocaRecyclerViewAdapter private constructor(private val activity: AppCompatActivity)
    : RecyclerView.Adapter<VocaViewHolder?>(), OnDeleteModeListener {

    // Custom listener interfaces. Will be used in the ViewHolder below.
    interface OnVocaClickListener {
        fun onVocaClick(holder: VocaViewHolder?, view: View?, position: Int)
        fun onVocaLongClick(holder: VocaViewHolder?, view: View?, position: Int): Boolean
    }

    interface ShowVocaOnNotification {
        fun showVocabularyOnNotification(vocabulary: RoomVocabulary?)
    }

    interface OnSelectModeListener {
        fun onDeleteModeEnabled()
        fun onDeleteModeDisabled()
    }

    private val viewModelProvider: ViewModelProvider?

    private var vocaClickListener: OnVocaClickListener? = null
    private var showVocaOnNotification: ShowVocaOnNotification? = null

    private var currentVocabulary: MutableList<RoomVocabulary?>?
    private var newVocaViewModel: NewVocaViewModel

    private val selectedItems: SparseBooleanArray = SparseBooleanArray()

    private var deleteMode = false
    private var onDeleteModeListener: OnSelectModeListener? = null
    private var onVocabularyUpdateListener: OnVocabularyUpdateListener? = null
    private var searchMode = false

    private var handler: Handler? = null
    private val observeDelay = 400

    init {
        viewModelProvider = ViewModelProvider(activity)
        newVocaViewModel = ViewModelProvider(activity, NewVocaViewModelFactory(VocaPersistenceDatabase(activity))).get(NewVocaViewModel::class.java)
        currentVocabulary = newVocaViewModel.getAllVocabulary().toMutableList()
    }

    /* Notify adapter to show added item on screen immediately */
    fun observe() {
        if (handler == null) {
            handler = Handler()
        }
        handler!!.postDelayed({ notifyDataSetChanged() }, observeDelay.toLong())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocaViewHolder {
        val vocaBinding = VocaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val holder = VocaViewHolder(vocaBinding, this, onVocabularyUpdateListener)
        holder.setVocaClickListener(vocaClickListener)
        return holder
    }

    // Bind the content to the item
    override fun onBindViewHolder(holder: VocaViewHolder, position: Int) {
        if (currentVocabulary == null) {
            holder.setVocabulary(RoomVocabulary.nullVocabulary)
            holder.setVocaClickListener(vocaClickListener)
        } else {
            val vocabulary = currentVocabulary?.get(position)
            holder.setVocabulary(vocabulary)
            holder.setVocaClickListener(vocaClickListener)
            holder.vocaBinding.deleteCheckBox.apply {
                if (deleteMode) {
                    visibility = View.VISIBLE
                    isChecked = selectedItems.get(position)
                } else {
                    visibility = View.GONE
                    isChecked = false
                }
            }
        }
    }

    // this should be used only when setting observer to LiveData object
    fun getCurrentVocabulary() = currentVocabulary

    // Methods for general adapter
    override fun getItemCount() = currentVocabulary?.size ?: -1

    fun getItem(position: Int) = currentVocabulary?.get(position)

    override fun getItemId(position: Int) = position.toLong()

    fun getItemPosition(vocabulary: RoomVocabulary?) = currentVocabulary?.indexOf(vocabulary)

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

    fun getSelectedItems(): MutableList<Int?> {
        val items: MutableList<Int?> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    fun setDeleteMode(deleteMode: Boolean) {
        this.deleteMode = deleteMode
    }

    fun isDeleteMode() = deleteMode

    fun notifyItemsChanged() {
        for (i in currentVocabulary?.indices!!) {
            notifyItemChanged(i)
        }
    }

    // for search mode
    fun enableSearchMode() {
        searchMode = true
    }

    fun disableSearchMode() {
        if (searchMode) {
            searchMode = false
            currentVocabulary = newVocaViewModel.getAllVocabulary().toMutableList()
            sortItems(sortState)
            notifyDataSetChanged()
        }
    }

    fun searchVocabulary(query: String?) {
        currentVocabulary = newVocaViewModel.getVocabulary("%$query%")?.toMutableList()
        Log.d(AppHelper.LOG_TAG, "Searched $query: ${currentVocabulary?.size ?: -1}")
        sortItems(sortState)
        notifyDataSetChanged()
    }

    // for remove and restore item with swiping
    fun removeItem(position: Int) {
        val deletedVocabulary = currentVocabulary?.get(position)
        currentVocabulary?.removeAt(position)
        notifyItemRemoved(position)
        if (deletedVocabulary != null) {
            newVocaViewModel.deleteVocabulary(deletedVocabulary)
        }
    }

    fun deleteVocabulary() {
        val selected = getSelectedItems()
        // reverse iteration
        val iterator: MutableListIterator<*> = selected.listIterator(selected.size)
        while (iterator.hasPrevious()) {
            val vocabulary = getItem(iterator.previous() as Int)
            if (vocabulary != null) {
                newVocaViewModel.deleteVocabulary(vocabulary)
            }
        }
        if (isDeleteMode()) {
            disableDeleteMode()
        }
    }

    fun restoreItem(vocabulary: RoomVocabulary?, position: Int) {
        currentVocabulary?.add(position, vocabulary)
        notifyItemInserted(position)
        if (vocabulary != null) {
            newVocaViewModel.insertVocabulary(vocabulary)
        }
    }

    // Sort items
    // state 0: sort alphabetically
    // state 1: sort by latest edited time
    fun sortItems(method: Int) {
        currentVocabulary?.apply {
            sortState = method
            when (sortState) {
                0 -> this.sortBy { it?.eng }
                1 -> this.sortBy { it?.addedTime }
                else -> {
                    sortState = 0
                    Log.d(AppHelper.LOG_TAG, "정렬할 수 없습니다: method $method")
                }
            }
        }
        notifyDataSetChanged()
    }

    // See SeeAllFragment.onDeleteModeEnabled() Method
    override fun enableDeleteMode() {
        Log.d("HSK APP", "DELETE MODE ENABLED")
        setDeleteMode(true)
        notifyItemsChanged()
        onDeleteModeListener?.onDeleteModeEnabled()
    }

    // See SeeAllFragment.onDeleteModeDisabled() Method
    override fun disableDeleteMode() {
        Log.d("HSK APP", "DELETE MODE DISABLED")
        setDeleteMode(false)
        onDeleteModeListener?.onDeleteModeDisabled()
    }

    /**
     * ViewHolder for vocabulary object.
     * Manages the content of the item and action when the item is clicked or long-clicked.
     */
    inner class VocaViewHolder(val vocaBinding: VocaViewBinding,
                               onDeleteModeListener: OnDeleteModeListener?,
                               onVocabularyUpdateListener: OnVocabularyUpdateListener?) : RecyclerView.ViewHolder(vocaBinding.root), OnCreateContextMenuListener {

        var vocaClickListener: OnVocaClickListener? = null
        var onDeleteModeListener: OnDeleteModeListener?
        var onVocabularyUpdateListener: OnVocabularyUpdateListener?
        var viewForeground = vocaBinding.viewForeground
        var viewBackground = vocaBinding.viewBackground

        // long-click listener
        private val onMenuItemClickListener: MenuItem.OnMenuItemClickListener = MenuItem.OnMenuItemClickListener { item ->
            val position = adapterPosition
            val vocabulary = currentVocabulary?.get(position)
            Log.d("HSK APP", position.toString())
            when (item.itemId) {
                Constants.EDIT_CODE -> {
                    if (vocabulary != null) {
                        Log.d("HSK APP", "edit: ${vocabulary.eng}")
                    }
                    onVocabularyUpdateListener?.editVocabulary(position, vocabulary)
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
            if (getInstance(activity)?.deleteMode == true) {
                return
            }
            val edit = menu?.add(Menu.NONE, Constants.EDIT_CODE, 1, "수정")
            val delete = menu?.add(Menu.NONE, Constants.DELETE_CODE, 2, "삭제")
            //            MenuItem showOnNotification = menu.add(Menu.NONE, Constants.SHOW_ON_NOTIFICATION_CODE, 3, "알림에 보이기");
            edit?.setOnMenuItemClickListener(onMenuItemClickListener)
            delete?.setOnMenuItemClickListener(onMenuItemClickListener)
            //            showOnNotification.setOnMenuItemClickListener(onMenuItemClickListener);
        }

        fun setVocabulary(vocabulary: RoomVocabulary?) {
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

    companion object {
        private var instance: VocaRecyclerViewAdapter? = null
        private var sortState = 0
        fun getInstance(activity: AppCompatActivity?): VocaRecyclerViewAdapter? {
            if (instance == null) {
                synchronized(VocaRecyclerViewAdapter::class.java) { instance = VocaRecyclerViewAdapter(activity!!) }
            }
            return instance
        }
    }
}