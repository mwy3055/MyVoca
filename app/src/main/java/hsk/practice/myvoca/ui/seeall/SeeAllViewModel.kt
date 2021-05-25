package hsk.practice.myvoca.ui.seeall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.VocaRepository
import com.hsk.domain.vocabulary.Vocabulary
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabularyList
import hsk.practice.myvoca.framework.toRoomVocabularyMutableList
import hsk.practice.myvoca.framework.toVocabulary
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

    private val _allVocabulary = MutableLiveData<List<RoomVocabulary?>?>()
    val allVocabulary: LiveData<List<RoomVocabulary?>?>
        get() = _allVocabulary

    private val _currentVocabulary = MutableLiveData<MutableList<RoomVocabulary?>?>()
    val currentVocabulary: LiveData<MutableList<RoomVocabulary?>?>
        get() = _currentVocabulary

    private val _deleteMode = MutableLiveData(false)
    val deleteMode: LiveData<Boolean>
        get() = _deleteMode

    var searchMode = false

    // state 0: sort alphabetically
    // state 1: sort by latest edited time
    private val _sortState = MutableLiveData(SortMethod.ENG)
    val sortState: LiveData<SortMethod>
        get() = _sortState

    init {
        loadAllVocabulary()
    }

    private fun loadAllVocabulary() = viewModelScope.launch(Dispatchers.IO) {
        val allVocabularyFlow = vocaRepository.getAllVocabulary()
        allVocabularyFlow.collectLatest {
            _allVocabulary.postValue(it.toRoomVocabularyList())
            if (!searchMode) {
                val sortedList = sortItems(it, sortState.value!!)
                _currentVocabulary.postValue(sortedList.toRoomVocabularyMutableList())
            }
        }
    }

    fun getCurrentVocabulary(position: Int): RoomVocabulary? =
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
        val result = vocaRepository.getVocabulary(query) ?: return@launch
        val sortedResult = sortItems(result, sortState.value!!)
        _currentVocabulary.postValue(sortedResult.toRoomVocabularyMutableList())
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

    fun restoreItem(target: RoomVocabulary) = viewModelScope.launch(Dispatchers.IO) {
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
                SortMethod.ENG -> this.sortBy { it?.eng }
                SortMethod.EDITED_TIME -> this.sortByDescending { it?.addedTime }
                else -> {
                    Logger.d("정렬할 수 없습니다: method ${sortState.value}")
                }
            }
        }
    }

    fun sortItems(items: List<Vocabulary?>, method: SortMethod): List<Vocabulary?> {
        return when (method) {
            SortMethod.ENG -> items.sortedBy { it?.eng }
            SortMethod.EDITED_TIME -> items.sortedByDescending { it?.addedTime }
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

    fun onDeleteModeChange(mode: Boolean) {
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
    private val _eventShowVocabulary = MutableLiveData<RoomVocabulary?>()
    val eventShowVocabulary: LiveData<RoomVocabulary?>
        get() = _eventShowVocabulary

    fun onShowVocabulary(vocabulary: RoomVocabulary) {
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

    val menuItemClickListener = object : VocaRecyclerViewAdapter.OnMenuItemClickListener {
        override fun onClick(itemId: Int, position: Int) {
            when (MenuCode.get(itemId)) {
                MenuCode.EDIT -> {
                    onVocabularyUpdate(position)
                }
                MenuCode.DELETE -> {
                    onDeleteModeChange(true)
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