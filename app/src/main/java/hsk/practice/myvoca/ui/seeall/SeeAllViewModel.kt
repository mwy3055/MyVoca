package hsk.practice.myvoca.ui.seeall

import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.VocaRepository
import com.hsk.domain.vocabulary.Vocabulary
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocabularyImpl
import hsk.practice.myvoca.framework.toVocabulary
import hsk.practice.myvoca.framework.toVocabularyImpl
import hsk.practice.myvoca.framework.toVocabularyImplList
import hsk.practice.myvoca.module.RoomVocaRepository
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortMethod(val value: Int) {
    ENG(0),
    EDITED_TIME(1);

    companion object {
        private val VALUES = values()
        fun get(value: Int) = VALUES.firstOrNull { it.value == value }
    }
}

@HiltViewModel
class SeeAllViewModel @Inject constructor(@RoomVocaRepository private val vocaRepository: VocaRepository) :
    ViewModel() {

    private val _allVocabulary = MutableLiveData<List<VocabularyImpl>>()
    val allVocabulary: LiveData<List<VocabularyImpl>>
        get() = _allVocabulary

    private val _currentVocabulary = MutableLiveData<MutableList<VocabularyImpl>>()
    val currentVocabulary: LiveData<MutableList<VocabularyImpl>>
        get() = _currentVocabulary

    private val _deleteMode = MutableLiveData(false)
    val deleteMode: LiveData<Boolean>
        get() = _deleteMode

    var searchMode = false

    private val _sortState = MutableLiveData(SortMethod.ENG)
    val sortState: LiveData<SortMethod>
        get() = _sortState

    init {
        loadAllVocabulary()
    }

    private fun loadAllVocabulary() = viewModelScope.launch(Dispatchers.IO) {
        val allVocabularyFlow = vocaRepository.getAllVocabulary()
        allVocabularyFlow.collectLatest {
            _allVocabulary.postValue(it.toVocabularyImplList())
            if (!searchMode) {
                val sortedList = sortItems(it, sortState.value!!)
                _currentVocabulary.postValue(sortedList.toVocabularyImplList().toMutableList())
            }
        }
    }

    fun getCurrentVocabulary(position: Int): VocabularyImpl? =
        currentVocabulary.value?.get(position)

    fun enableSearchMode() {
        searchMode = true
    }

    fun disableSearchMode() {
        if (searchMode) {
            searchMode = false
            _currentVocabulary.value = allVocabulary.value?.toMutableList()
            sortItems() // TODO: what is this?
        }
    }

    /**
     * Searches the vocabulary with a given query.
     *
     * @param query String to search with. [query] should not include % character.
     */
    fun searchVocabulary(query: String) = viewModelScope.launch(Dispatchers.IO) {
        val result = vocaRepository.getVocabulary(query)
        val sortedResult = sortItems(result, sortState.value!!)
        val sortedMutableList = sortedResult.map {
            it.toVocabularyImpl()
        }.toMutableList()
        _currentVocabulary.postValue(sortedMutableList)
//        sortItems(sortState) // TODO: what is this?
    }


    fun deleteItem(position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val target = currentVocabulary.value?.get(position) ?: return@launch
        vocaRepository.deleteVocabulary(target.toVocabulary())
    }

    fun deleteItems(targetIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
        for (targetIndex in targetIndices.asReversed()) {
            deleteItem(targetIndex)
        }
    }

    fun restoreItem(target: VocabularyImpl) = viewModelScope.launch(Dispatchers.IO) {
        vocaRepository.insertVocabulary(target.toVocabulary())
    }

    fun setSortState(method: SortMethod) {
        _sortState.value = method
        sortItems()
    }

    /**
     * Sort list according to the value of the sortState
     */
    fun sortItems() = viewModelScope.launch {
        _currentVocabulary.value?.apply {
            when (sortState.value) {
                SortMethod.ENG -> this.sortBy { it.eng }
                SortMethod.EDITED_TIME -> this.sortByDescending { it.addedTime }
                else -> {
                    Logger.d("정렬할 수 없습니다: method ${sortState.value}")
                }
            }
        }
    }

    fun sortItems(items: List<Vocabulary>, method: SortMethod): List<Vocabulary> {
        return when (method) {
            SortMethod.ENG -> items.sortedBy { it.eng }
            SortMethod.EDITED_TIME -> items.sortedByDescending { it.addedTime }
        }
    }

    /**
     * LiveData object for delivering event.
     * Fired when vocabulary is updated in the RecyclerView.
     */
    private val _eventVocabularyUpdated = MutableLiveData<Int?>()
    val eventVocabularyUpdateRequest: LiveData<Int?>
        get() = _eventVocabularyUpdated

    fun onVocabularyUpdate(position: Int) {
        _eventVocabularyUpdated.value = position
    }

    fun onVocabularyUpdateComplete() {
        _eventVocabularyUpdated.value = null
    }

    /**
     * LiveData object for delivering event.
     * Fired when delete mode is changed at the RecyclerView.
     */
    private val _eventDeleteModeChanged = MutableLiveData<Boolean?>()
    val eventDeleteModeChanged: LiveData<Boolean?>
        get() = _eventDeleteModeChanged

    fun changeDeleteMode(mode: Boolean) {
        _eventDeleteModeChanged.value = mode
        _deleteMode.value = mode
    }

    fun onDeleteModeUpdateComplete() {
        _eventDeleteModeChanged.value = null
    }

    /**
     * LiveData object for delivering event.
     * Fired when user wants to show a vocabulary in the notification.
     */
    private val _eventShowVocabulary = MutableLiveData<VocabularyImpl?>()
    val eventShowVocabulary: LiveData<VocabularyImpl?>
        get() = _eventShowVocabulary

    fun onShowVocabulary(vocabulary: VocabularyImpl) {
        _eventShowVocabulary.value = vocabulary
    }

    fun onShowVocabularyComplete() {
        _eventShowVocabulary.value = null
    }

    /**
     * Data for RecyclerView.
     *
     * Manages selected items in a RecyclerView when delete mode is enabled.
     */
    private val selectedItems = mutableSetOf<Int>()

    fun isSelected(position: Int) = selectedItems.contains(position)

    fun switchSelectedState(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
    }

    fun clearSelectedItems() {
        selectedItems.clear()
    }

    fun deleteSelectedItems() {
        deleteItems(selectedItems.toList())
    }

    fun getSelectedItems() = selectedItems

    fun getSelectedItemsList(): List<Int> = selectedItems.toList()

    fun getSelectedCount() = selectedItems.size

    /**
     * Listener for each item in VocaRecyclerView. See [VocaRecyclerViewAdapter.ItemListener].
     */
    val itemListener = object : VocaRecyclerViewAdapter.ItemListener {
        override fun onRootClick(view: View, position: Int) {
            if (deleteMode.value == true) {
                val checkBox = view.findViewById<CheckBox>(R.id.delete_check_box)
                val isChecked = checkBox.isChecked
                checkBox.isChecked = !isChecked
                onDeleteCheckBoxClick(checkBox, position)
            }
        }

        override fun onDeleteCheckBoxClick(view: View, position: Int) {
            if (deleteMode.value == true) {
                switchSelectedState(position)
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            view: View,
            contextMenuInfo: ContextMenu.ContextMenuInfo?,
            position: Int
        ) {
            val realMenuItemClickListener = MenuItem.OnMenuItemClickListener { item ->
                onMenuItemClick(item.itemId, position)
                true
            }

            if (deleteMode.value == false) {
                menu.add(Menu.NONE, MenuCode.EDIT.value, 1, "수정")?.apply {
                    setOnMenuItemClickListener(realMenuItemClickListener)
                }
                menu.add(Menu.NONE, MenuCode.DELETE.value, 2, "삭제")?.apply {
                    setOnMenuItemClickListener(realMenuItemClickListener)
                }
                menu.add(Menu.NONE, MenuCode.SHOW_ON_NOTIFICATION.value, 3, "알림에 보이기")
                    ?.apply {
                        setOnMenuItemClickListener(realMenuItemClickListener)
                    }
            }
        }

        /**
         * Callback invoked when each menu item is clicked
         *
         * @param itemId Id of the menu item
         * @param position Position of the RecyclerView item
         */
        fun onMenuItemClick(itemId: Int, position: Int) {
            when (MenuCode.get(itemId)) {
                MenuCode.EDIT -> {
                    onVocabularyUpdate(position)
                }
                MenuCode.DELETE -> {
                    changeDeleteMode(true)
                    switchSelectedState(position)
                }
                MenuCode.SHOW_ON_NOTIFICATION -> {
                    val vocabulary = getCurrentVocabulary(position)
                    vocabulary?.let { onShowVocabulary(it) }
                }
                else -> Logger.d("MenuItemClick Else")
            }
        }
    }

    /**
     * Delete data for VocaRecyclerView. See [VocaRecyclerViewAdapter.DeleteData].
     */
    val deleteData = object : VocaRecyclerViewAdapter.DeleteData {
        override val deleteMode: LiveData<Boolean>
            get() = this@SeeAllViewModel.deleteMode

        override fun isSelected(position: Int): Boolean = selectedItems.contains(position)
    }
}

/**
 * Menu item code in RecyclerView.
 * Assigned one-by-one for each menu item.
 */
enum class MenuCode(val value: Int) {
    EDIT(0),
    DELETE(1),
    SHOW_ON_NOTIFICATION(2);

    companion object {
        private val VALUES = values()
        fun get(value: Int) = VALUES.firstOrNull { it.value == value }
    }
}