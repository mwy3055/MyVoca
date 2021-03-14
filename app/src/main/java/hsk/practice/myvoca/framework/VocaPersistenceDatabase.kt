package hsk.practice.myvoca.framework

import android.content.Context
import com.hsk.data.VocaPersistence
import com.hsk.domain.vocabulary.Vocabulary
import hsk.practice.myvoca.SingletonHolder
import hsk.practice.myvoca.containsOnlyAlphabet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Vocabulary Persistence Room Database.
 * Implemented as singleton to keep the data persistence across the whole app.
 */
class VocaPersistenceDatabase private constructor(context: Context) : VocaPersistence, CoroutineScope {

    companion object : SingletonHolder<VocaPersistenceDatabase, Context>(::VocaPersistenceDatabase)

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private var databaseRoom: RoomVocaDatabase
    private val vocaDao: VocaDao
        get() = databaseRoom.vocaDao()!!

    private val _allVocabulary = MutableStateFlow<List<Vocabulary?>>(emptyList())

    init {
        synchronized(this) {
            databaseRoom = RoomVocaDatabase.getInstance(context)
        }
        loadAllVocabulary()
        Timber.d("Here")
    }

    override fun getAllVocabulary(): StateFlow<List<Vocabulary?>> {
        Timber.d("getAllVocabulary called!")
        return _allVocabulary
    }

//    override suspend fun getAllVocabulary(): List<Vocabulary?> {
//        return allVocabulary.getOrAwaitValue().toVocabularyList()
//    }

    private fun loadAllVocabulary() = launch {
        vocaDao.loadAllVocabulary().collect {
            Timber.d("AllVocabulary loaded!")
            _allVocabulary.value = it.toVocabularyList()
        }
    }

    override suspend fun getVocabulary(query: String): List<Vocabulary?>? {
        return if (query.containsOnlyAlphabet()) {
            vocaDao.loadVocabularyByEng(query)
        } else {
            vocaDao.loadVocabularyByKor(query)
        }?.toVocabularyList()
    }

    override suspend fun deleteVocabulary(vararg vocabularies: Vocabulary?) {
        vocaDao.deleteVocabulary(*vocabularies.toRoomVocabularyArray())
    }

    override suspend fun updateVocabulary(vararg vocabularies: Vocabulary?) {
        vocaDao.updateVocabulary(*vocabularies.toRoomVocabularyArray())
    }

    override suspend fun insertVocabulary(vararg vocabularies: Vocabulary?) {
        vocaDao.insertVocabulary(*vocabularies.toRoomVocabularyArray())
    }
}
