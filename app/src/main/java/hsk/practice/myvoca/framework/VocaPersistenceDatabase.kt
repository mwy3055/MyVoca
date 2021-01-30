package hsk.practice.myvoca.framework

import android.content.Context
import com.hsk.data.VocaPersistence
import com.hsk.domain.vocabulary.Vocabulary
import hsk.practice.myvoca.containsOnlyAlphabet
import kotlinx.coroutines.runBlocking

class VocaPersistenceDatabase(context: Context) : VocaPersistence {

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
                vocaDao.loadAllVocabulary()?.value.toVocabularyList() ?: emptyList()
            }
        }
        return allVocabulary
    }

    override fun getVocabulary(query: String): List<Vocabulary?>? {
        val resultList = runBlocking {
            if (query.containsOnlyAlphabet()) {
                vocaDao.loadVocabularyByEng(query)?.value.toVocabularyList()
            } else {
                vocaDao.loadVocabularyByKor(query)?.value.toVocabularyList()
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