package hsk.practice.myvoca.framework

import android.content.Context
import com.hsk.data.VocaPersistence
import com.hsk.domain.vocabulary.Vocabulary
import hsk.practice.myvoca.SingletonHolder
import hsk.practice.myvoca.containsOnlyAlphabet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Vocabulary Persistence Room Database.
 * Implemented as singleton to keep the data persistence across the whole app.
 */
class VocaPersistenceDatabase private constructor(context: Context) : VocaPersistence {

    companion object : SingletonHolder<VocaPersistenceDatabase, Context>(::VocaPersistenceDatabase)

    private var databaseRoom: RoomVocaDatabase
    private val vocaDao: VocaDao
        get() = databaseRoom.vocaDao()!!

    private val allVocabularyChannel = Channel<List<Vocabulary?>>()

    private val allVocabulary: Flow<List<RoomVocabulary>>
    private var isVocabularyLoading = false

    init {
        synchronized(this) {
            databaseRoom = RoomVocaDatabase.getInstance(context)
        }
        allVocabulary = vocaDao.loadAllVocabulary()
        Timber.d("Here")
    }

    override fun getAllVocabulary() = allVocabulary.map { it.toVocabularyList() }

//    override suspend fun getAllVocabulary(): List<Vocabulary?> {
//        return allVocabulary.getOrAwaitValue().toVocabularyList()
//    }

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