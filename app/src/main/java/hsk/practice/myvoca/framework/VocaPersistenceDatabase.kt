package hsk.practice.myvoca.framework

import android.content.Context
import android.util.Log
import com.hsk.data.VocaPersistence
import com.hsk.domain.vocabulary.Vocabulary
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.containsOnlyAlphabet
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class VocaPersistenceDatabase(context: Context?) : VocaPersistence {

    private var databaseRoom: RoomVocaDatabase
    private val vocaDao: VocaDao
        get() = databaseRoom.vocaDao()!!

    private lateinit var allVocabulary: List<Vocabulary?>

    init {
        synchronized(this::class.java) {
            databaseRoom = RoomVocaDatabase.getInstance(context)
        }
    }

    override fun getAllVocabulary(): List<Vocabulary?> {
        if (!::allVocabulary.isInitialized) {
            allVocabulary = runBlocking {
                // TODO: How to mix LiveData with coroutine
                val list = vocaDao.loadAllVocabulary()?.toVocabularyList() ?: emptyList()
                Log.d(AppHelper.LOG_TAG, "list empty? ${list.isEmpty()}")
                delay(1000)
                list
            }
        }
        Log.d(AppHelper.LOG_TAG, "allVocabulary empty? ${allVocabulary.isEmpty()}")
        return allVocabulary
    }

    override fun getVocabulary(query: String): List<Vocabulary?>? {
        val resultList = runBlocking {
            if (query.containsOnlyAlphabet()) {
                vocaDao.loadVocabularyByEng(query)?.toVocabularyList()
            } else {
                vocaDao.loadVocabularyByKor(query)?.toVocabularyList()
            }
        }
        return resultList
    }

    override fun deleteVocabulary(vararg vocabularies: Vocabulary?) {
        runBlocking {
            vocaDao.deleteVocabulary(*vocabularies.toRoomVocabularyArray())
        }
    }

    override fun updateVocabulary(vararg vocabularies: Vocabulary?) {
        runBlocking {
            vocaDao.updateVocabulary(*vocabularies.toRoomVocabularyArray())
        }
    }

    override fun insertVocabulary(vararg vocabularies: Vocabulary?) {
        runBlocking {
            vocaDao.insertVocabulary(*vocabularies.toRoomVocabularyArray())
        }
    }
}