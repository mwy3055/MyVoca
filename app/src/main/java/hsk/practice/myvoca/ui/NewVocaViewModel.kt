package hsk.practice.myvoca.ui

import androidx.lifecycle.*
import com.hsk.data.VocaPersistence
import com.hsk.data.VocaRepository
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabularyMutableList
import hsk.practice.myvoca.framework.toVocabularyArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    private var allVocabulary: LiveData<MutableList<RoomVocabulary?>?>?=null
    private var vocabularyCount: MutableLiveData<Int>?=null

    init {
        loadVocabulary()
        allVocabulary?.observeForever {
            vocabularyCount = MutableLiveData()
            vocabularyCount!!.value = it?.size ?: 0
        }
    }

    private val defaultContext: CoroutineContext
        get() = viewModelScope.coroutineContext + Dispatchers.Default

    private val ioContext: CoroutineContext
        get() = viewModelScope.coroutineContext + Dispatchers.IO

    @Synchronized
    private fun loadVocabulary(): LiveData<MutableList<RoomVocabulary?>?> = liveData {
        emit(vocaRepository.getAllVocabulary().toRoomVocabularyMutableList())
    }

    fun getAllVocabulary(): LiveData<MutableList<RoomVocabulary?>?>? {
        if (allVocabulary?.value==null) {
            allVocabulary = loadVocabulary()
        }
        return allVocabulary
    }

    fun getVocabularyCount(): LiveData<Int>? = vocabularyCount

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

    fun getRandomVocabulary() = liveData {
        emit(vocaRepository.getRandomVocabulary()?.toRoomVocabulary())
    }

    fun getRandomVocabularies(count: Int, notInclude: RoomVocabulary? = null) = liveData {
        var result = mutableSetOf<RoomVocabulary?>()
        while (result.size < count) {
            val tempList = (1..10).map {
                viewModelScope.async {
                    getRandomVocabulary()
                }
            }.awaitAll().map {
                it.value
            }
            result = (result + tempList).filter { it != notInclude }.toMutableSet()
        }
        emit(result.shuffled().subList(0, count))
    }

    fun isEmpty() = liveData {
        emit(allVocabulary?.value?.isEmpty() ?: true)
    }

}