package hsk.practice.myvoca.ui

import androidx.lifecycle.*
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabularyMutableList
import hsk.practice.myvoca.framework.toVocabularyArray
import hsk.practice.myvoca.module.RoomVocaRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * VocaViewModel is at the top of the database abstraction.
 * ViewModel interacts with the UI classes directly.
 * All database operations must be done through this class.
 *
 * Also, all methods work asynchronously because database access is costly.
 * Methods return the LiveData immediately when the method is called. Actual result will be filled into LiveData later.
 * UI classes should observe the LiveData and define what to do when the operation is actually finished.
 */
@HiltViewModel
class NewVocaViewModel @Inject constructor(@RoomVocaRepository private val vocaRepository: VocaRepository) :
    ViewModel() {

    val allVocabulary: LiveData<MutableList<RoomVocabulary?>?> = Transformations.map(
        vocaRepository.getAllVocabulary().asLiveData(viewModelScope.coroutineContext)
    ) {
        Logger.d("allVocabulary assigned")
        it.toRoomVocabularyMutableList()
    }

    fun getVocabularyCount(): LiveData<Int> = Transformations.map(allVocabulary) {
        Logger.d("Size: ${it?.size}")
        it?.size ?: 0
    }

    fun getVocabulary(query: String) = liveData {
        emit(vocaRepository.getVocabulary(query).toRoomVocabularyMutableList())
    }

    fun insertVocabulary(vararg vocabularies: RoomVocabulary) = viewModelScope.launch {
        vocaRepository.insertVocabulary(*vocabularies.toVocabularyArray())
    }

    fun deleteVocabulary(vararg vocabularies: RoomVocabulary) = viewModelScope.launch {
        vocaRepository.deleteVocabulary(*vocabularies.toVocabularyArray())
    }

    fun updateVocabulary(vararg vocabularies: RoomVocabulary) = viewModelScope.launch {
        vocaRepository.updateVocabulary(*vocabularies.toVocabularyArray())
    }

    fun getRandomVocabulary(): LiveData<RoomVocabulary> = Transformations.map(allVocabulary) {
        it?.random() ?: RoomVocabulary.nullVocabulary
    }

    fun getRandomVocabularySync(): RoomVocabulary =
        allVocabulary.value?.random() ?: RoomVocabulary.nullVocabulary

    fun isEmpty(): LiveData<Boolean> = Transformations.map(allVocabulary) {
        it.isNullOrEmpty()
    }

}