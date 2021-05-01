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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeeAllViewModel @Inject constructor(@RoomVocaRepository private val vocaRepository: VocaRepository) : ViewModel() {

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
    private val _sortState = MutableLiveData(0)
    val sortState: LiveData<Int>
        get() = _sortState

    init {
        loadAllVocabulary()
    }

    private fun loadAllVocabulary() = viewModelScope.launch(Dispatchers.IO) {
        val allVocabularyFlow = vocaRepository.getAllVocabulary()
        allVocabularyFlow.collectLatest {
            _allVocabulary.postValue(it.toRoomVocabularyList())
            if (!searchMode) {
                _currentVocabulary.postValue(it.toRoomVocabularyMutableList())
            }
        }
    }

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

    fun setSortState(method: Int) {
        if (method in 0..1) {
            _sortState.value = method
            sortItems()
        } else {
            throw IllegalArgumentException("Wrong sort method: $method")
        }
    }

    /**
     * Sort list according to the value of the sortState
     */
    fun sortItems() = viewModelScope.launch {
        _currentVocabulary.value?.apply {
            when (sortState.value) {
                0 -> this.sortBy { it?.eng }
                1 -> this.sortByDescending { it?.addedTime }
                else -> {
                    Logger.d("정렬할 수 없습니다: method ${sortState.value}")
                }
            }
        }
    }


    fun sortItems(items: List<Vocabulary?>, method: Int): List<Vocabulary?> {
        return when (method) {
            0 -> items.sortedBy { it?.eng }
            1 -> items.sortedByDescending { it?.addedTime }
            else -> {
                Logger.d("정렬할 수 없습니다: method $method")
                items
            }
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

}