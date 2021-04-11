package hsk.practice.myvoca.ui

import androidx.lifecycle.*
import com.hsk.data.VocaPersistence
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabularyMutableList
import hsk.practice.myvoca.framework.toVocabularyArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * VocaViewModel is at the top of the database abstraction.
 * ViewModel interacts with the UI classes directly.
 * All database operations must be done through this class.
 *
 * Also, all methods work asynchronously because database access is costly.
 * Methods return the LiveData immediately when the method is called. Actual result will be filled into LiveData later.
 * UI classes should observe the LiveData and define what to do when the operation is actually finished.
 */
class NewVocaViewModel(vocaPersistence: VocaPersistence) : ViewModel() {

    private var vocaRepository: VocaRepository = VocaRepository(vocaPersistence)

    val allVocabulary: LiveData<MutableList<RoomVocabulary?>?>

    init {
        allVocabulary = loadVocabulary()
    }

    private val defaultContext: CoroutineContext
        get() = viewModelScope.coroutineContext + Dispatchers.Default

    private val ioContext: CoroutineContext
        get() = viewModelScope.coroutineContext + Dispatchers.IO

    @Synchronized
    private fun loadVocabulary() = Transformations.map(vocaRepository.getAllVocabulary().asLiveData(viewModelScope.coroutineContext)) {
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

    fun isEmpty(): LiveData<Boolean> = Transformations.map(allVocabulary) {
        it.isNullOrEmpty()
    }

}