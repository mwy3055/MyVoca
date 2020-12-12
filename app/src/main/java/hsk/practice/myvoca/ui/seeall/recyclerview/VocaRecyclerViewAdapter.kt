package hsk.practice.myvoca.ui.seeall.recyclerview

import android.os.*
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.View.OnCreateContextMenuListener
import android.view.View.OnLongClickListener
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import database.VocaComparator
import database.Vocabulary
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocaViewModel
import hsk.practice.myvoca.ui.seeall.OnDeleteModeListener
import hsk.practice.myvoca.ui.seeall.OnEditVocabularyListener
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.VocaViewHolder
import java.util.*

/**
 * RecyclerAdapter for each vocabulary.
 * Implemented as Singleton to manage the view more easily.
 *
 * For further information, Please refer the comments above some methods.
 */
class VocaRecyclerViewAdapter private constructor(private val activity: AppCompatActivity?) : RecyclerView.Adapter<VocaViewHolder?>(), OnDeleteModeListener {
    // Custom listener interfaces. Will be used in the ViewHolder below.
    interface OnVocaClickListener {
        open fun onVocaClick(holder: VocaViewHolder?, view: View?, position: Int)
        open fun onVocaLongClick(holder: VocaViewHolder?, view: View?, position: Int): Boolean
    }

    interface showVocaOnNotification {
        open fun showVocabularyOnNotification(vocabulary: Vocabulary?)
    }

    interface OnSelectModeListener {
        open fun onDeleteModeEnabled()
        open fun onDeleteModeDisabled()
    }

    private val viewModelProvider: ViewModelProvider?
    private var vocaClickListener: OnVocaClickListener? = null
    private var showVocaOnNotification: showVocaOnNotification? = null
    private var currentVocabulary: LiveData<MutableList<Vocabulary?>?>?
    private val vocaViewModel: VocaViewModel?
    private val selectedItems: SparseBooleanArray? = SparseBooleanArray()
    private var deleteMode = false
    private var onDeleteModeListener: OnSelectModeListener? = null
    private var onEditVocabularyListener: OnEditVocabularyListener? = null
    private var searchMode = false
    private var handler: Handler? = null
    private val observeDelay = 400

    /* Notify adapter to show added item on screen immediately */
    fun observe() {
        if (handler == null) {
            handler = Handler()
        }
        handler.postDelayed(Runnable { notifyDataSetChanged() }, observeDelay.toLong())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocaViewHolder {
        val view = VocaView(activity)
        val holder = VocaViewHolder(view, this, onEditVocabularyListener)
        holder.setVocaClickListener(vocaClickListener)
        return holder
    }

    // Bind the content to the item
    override fun onBindViewHolder(holder: VocaViewHolder, position: Int) {
        if (currentVocabulary.getValue() == null) {
            holder.setVocabulary(Vocabulary("null", "널입니다.", (System.currentTimeMillis() / 1000) as Int, (System.currentTimeMillis() / 1000) as Int, ""))
            holder.setVocaClickListener(vocaClickListener)
            return
        }
        val vocabulary = currentVocabulary.getValue().get(position)
        holder.setVocabulary(vocabulary)
        holder.setVocaClickListener(vocaClickListener)
        val checkBox = holder.vocaView.deleteCheckBox
        if (deleteMode) {
            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = selectedItems.get(position)
            holder.vocaView.invalidate()
        } else {
            checkBox.visibility = View.GONE
            checkBox.isChecked = false
        }
    }

    // this should be used only when setting observer to LiveData object
    fun getCurrentVocabulary(): LiveData<MutableList<Vocabulary?>?>? {
        return currentVocabulary
    }

    // Methods for general adapter
    override fun getItemCount(): Int {
        return if (currentVocabulary.getValue() == null) {
            -1
        } else currentVocabulary.getValue().size
    }

    fun getItem(position: Int): Any? {
        return currentVocabulary.getValue().get(position)
    }

    override fun getItemId(position: Int): Long {
        return position as Long
    }

    fun getItemPosition(vocabulary: Vocabulary?): Int {
        return currentVocabulary.getValue().indexOf(vocabulary)
    }

    // Getter/Setters of Listeners
    fun getVocaClickListener(): OnVocaClickListener? {
        return vocaClickListener
    }

    fun setOnDeleteModeListener(listener: OnSelectModeListener?) {
        onDeleteModeListener = listener
    }

    fun setVocaClickListener(vocaClickListener: OnVocaClickListener?) {
        this.vocaClickListener = vocaClickListener
    }

    fun setOnEditVocabularyListener(listener: OnEditVocabularyListener?) {
        onEditVocabularyListener = listener
    }

    fun setShowVocaOnNotificationListener(notificationListener: showVocaOnNotification?) {
        showVocaOnNotification = notificationListener
    }

    // Methods for managing select state
    fun isSelected(position: Int): Boolean {
        return getSelectedItems().contains(position)
    }

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

    fun getSelectedItemCount(): Int {
        return selectedItems.size()
    }

    fun getSelectedItems(): MutableList<Int?>? {
        val items: MutableList<Int?> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    fun setDeleteMode(deleteMode: Boolean) {
        this.deleteMode = deleteMode
    }

    fun isDeleteMode(): Boolean {
        return deleteMode
    }

    fun notifyItemsChanged() {
        for (i in currentVocabulary.getValue().indices) {
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
            currentVocabulary = vocaViewModel.getAllVocabulary()
            sortItems(sortState)
            notifyDataSetChanged()
        }
    }

    fun searchVocabulary(query: String?) {
        currentVocabulary = vocaViewModel.getVocabulary("%$query%")
        currentVocabulary.observe(activity, Observer {
            Log.d("HSK APP", "Searched " + query + ": " + if (currentVocabulary.getValue() == null) -1 else currentVocabulary.getValue().size)
            sortItems(sortState)
            notifyDataSetChanged()
        })
    }

    // for remove and restore item with swiping
    fun removeItem(position: Int) {
        val deletedVocabulary = currentVocabulary.getValue().get(position)
        currentVocabulary.getValue().removeAt(position)
        notifyItemRemoved(position)
        vocaViewModel.deleteVocabulary(deletedVocabulary)
    }

    fun deleteVocabulary() {
        val selected = getSelectedItems()
        // reverse iteration
        val iterator: MutableListIterator<*> = selected.listIterator(selected.size)
        while (iterator.hasPrevious()) {
            val vocabulary = getItem(iterator.previous() as Int) as Vocabulary?
            vocaViewModel.deleteVocabulary(vocabulary)
        }
        if (isDeleteMode()) {
            disableDeleteMode()
        }
    }

    fun restoreItem(vocabulary: Vocabulary?, position: Int) {
        currentVocabulary.getValue().add(position, vocabulary)
        notifyItemInserted(position)
        vocaViewModel.insertVocabulary(vocabulary)
    }

    // Sort items
    // state 0: sort alphabetically
    // state 1: sort by latest edited time
    fun sortItems(method: Int) {
        if (currentVocabulary == null || currentVocabulary.getValue() == null) {
            return
        }
        sortState = method
        if (sortState == 0) {
            Collections.sort(currentVocabulary.getValue(), VocaComparator.getEngComparator())
        } else if (sortState == 1) {
            Collections.sort(currentVocabulary.getValue(), VocaComparator.getAddedTimeComparator())
        } else {
            sortState = 0
            Toast.makeText(activity.getApplicationContext(), "정렬할 수 없습니다: $method", Toast.LENGTH_LONG).show()
        }
        notifyDataSetChanged()
    }

    // See SeeAllFragment.onDeleteModeEnabled() Method
    override fun enableDeleteMode() {
        Log.d("HSK APP", "DELETE MODE ENABLED")
        setDeleteMode(true)
        notifyItemsChanged()
        if (onDeleteModeListener != null) {
            onDeleteModeListener.onDeleteModeEnabled()
        }
    }

    // See SeeAllFragment.onDeleteModeDisabled() Method
    override fun disableDeleteMode() {
        Log.d("HSK APP", "DELETE MODE DISABLED")
        setDeleteMode(false)
        if (onDeleteModeListener != null) {
            onDeleteModeListener.onDeleteModeDisabled()
        }
    }

    /**
     * ViewHolder for vocabulary object.
     * Manages the content of the item and action when the item is clicked or long-clicked.
     */
    inner class VocaViewHolder(vocaView: View,
                               onDeleteModeListener: OnDeleteModeListener?,
                               onEditVocabularyListener: OnEditVocabularyListener?) : RecyclerView.ViewHolder(vocaView), OnCreateContextMenuListener {
        var vocaClickListener: OnVocaClickListener? = null
        var onDeleteModeListener: OnDeleteModeListener?
        var onEditVocabularyListener: OnEditVocabularyListener?
        var viewForeground: RelativeLayout?
        var viewBackground: RelativeLayout?

        // long-click listener
        private val onMenuItemClickListener: MenuItem.OnMenuItemClickListener? = MenuItem.OnMenuItemClickListener { item ->
            val position = adapterPosition
            val vocabulary = currentVocabulary.getValue().get(position)
            Log.d("HSK APP", Integer.toString(position))
            when (item.itemId) {
                Constants.EDIT_CODE -> {
                    Log.d("HSK APP", "edit: " + vocabulary.eng)
                    onEditVocabularyListener.editVocabulary(position, vocabulary)
                }
                Constants.DELETE_CODE -> {
                    val adapter = this@VocaRecyclerViewAdapter
                    adapter.enableDeleteMode()
                    adapter.switchSelectedState(position)
                }
                Constants.SHOW_ON_NOTIFICATION_CODE ->                         // TODO: show selected vocabulary on notification
                    showVocaOnNotification.showVocabularyOnNotification(vocabulary)
            }
            true
        }
        var vocaView: VocaView?

        // Create drop-down menu when item is long-clicked
        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
            if (getInstance(activity).deleteMode) {
                return
            }
            val edit = menu.add(Menu.NONE, Constants.EDIT_CODE, 1, "수정")
            val delete = menu.add(Menu.NONE, Constants.DELETE_CODE, 2, "삭제")
            //            MenuItem showOnNotification = menu.add(Menu.NONE, Constants.SHOW_ON_NOTIFICATION_CODE, 3, "알림에 보이기");
            edit.setOnMenuItemClickListener(onMenuItemClickListener)
            delete.setOnMenuItemClickListener(onMenuItemClickListener)
            //            showOnNotification.setOnMenuItemClickListener(onMenuItemClickListener);
        }

        fun setVocabulary(vocabulary: Vocabulary?) {
            vocaView.setVocabulary(vocabulary)
        }

        fun setVocaClickListener(vocaClickListener: OnVocaClickListener?) {
            this.vocaClickListener = vocaClickListener
        }

        init {
            viewBackground = vocaView.findViewById(R.id.view_background)
            viewForeground = vocaView.findViewById(R.id.view_foreground)
            this.vocaView = vocaView as VocaView
            this.vocaView.setOnClickListener(View.OnClickListener { v ->
                if (vocaClickListener != null) {
                    val position = adapterPosition
                    vocaClickListener.onVocaClick(this@VocaViewHolder, v, position)
                }
            })
            this.vocaView.setOnLongClickListener(OnLongClickListener { v ->
                if (vocaClickListener != null) {
                    val position = adapterPosition
                    return@OnLongClickListener vocaClickListener.onVocaLongClick(this@VocaViewHolder, v, position)
                }
                false
            })
            this.onDeleteModeListener = onDeleteModeListener
            this.onEditVocabularyListener = onEditVocabularyListener
            vocaView.setOnCreateContextMenuListener(this)
        }
    }

    companion object {
        private var instance: VocaRecyclerViewAdapter? = null
        private var sortState = 0
        fun getInstance(activity: AppCompatActivity?): VocaRecyclerViewAdapter? {
            if (instance == null) {
                synchronized(VocaRecyclerViewAdapter::class.java) { instance = VocaRecyclerViewAdapter(activity) }
            }
            return instance
        }
    }

    init {
        viewModelProvider = ViewModelProvider(activity)
        vocaViewModel = viewModelProvider.get(VocaViewModel::class.java)
        currentVocabulary = vocaViewModel.getAllVocabulary()
    }
}