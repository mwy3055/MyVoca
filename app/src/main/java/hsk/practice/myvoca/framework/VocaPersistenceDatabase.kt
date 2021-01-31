package hsk.practice.myvoca.framework

import android.content.Context
import android.util.Log
import com.hsk.data.VocaPersistence
import com.hsk.domain.vocabulary.Vocabulary
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.containsOnlyAlphabet
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class VocaPersistenceDatabase(context: Context?) : VocaPersistence, CoroutineScope {

    private var databaseRoom: RoomVocaDatabase
    private val vocaDao: VocaDao
        get() = databaseRoom.vocaDao()!!

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private val ioContext: CoroutineContext
        get() = Dispatchers.IO

    private lateinit var allVocabulary: List<Vocabulary?>

    init {
        synchronized(this::class.java) {
            databaseRoom = RoomVocaDatabase.getInstance(context)
        }
    }

    override suspend fun getAllVocabulary(): List<Vocabulary?> = coroutineScope {
        if (!::allVocabulary.isInitialized) {
            allVocabulary = withContext(ioContext) {
                vocaDao.loadAllVocabulary()?.toVocabularyList() ?: emptyList()
            }
        }
        Log.d(AppHelper.LOG_TAG, "allVocabulary empty? ${allVocabulary.isEmpty()}")
        allVocabulary
    }

    override suspend fun getVocabulary(query: String): List<Vocabulary?>? = coroutineScope {
        withContext(ioContext) {
            if (query.containsOnlyAlphabet()) {
                vocaDao.loadVocabularyByEng(query)?.toVocabularyList()
            } else {
                vocaDao.loadVocabularyByKor(query)?.toVocabularyList()
            }
        }
    }

    override suspend fun deleteVocabulary(vararg vocabularies: Vocabulary?) = coroutineScope {
        vocaDao.deleteVocabulary(*vocabularies.toRoomVocabularyArray())
    }

    override suspend fun updateVocabulary(vararg vocabularies: Vocabulary?) = coroutineScope {
        vocaDao.updateVocabulary(*vocabularies.toRoomVocabularyArray())
    }

    override suspend fun insertVocabulary(vararg vocabularies: Vocabulary?) = coroutineScope {
        vocaDao.insertVocabulary(*vocabularies.toRoomVocabularyArray())
    }
}