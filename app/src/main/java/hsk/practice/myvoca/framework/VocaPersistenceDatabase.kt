package hsk.practice.myvoca.framework

import android.content.Context
import com.hsk.data.VocaPersistence
import com.hsk.domain.vocabulary.Vocabulary
import com.orhanobut.logger.Logger
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hsk.practice.myvoca.containsOnlyAlphabet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * Vocabulary Persistence Room Database.
 * Implemented as singleton to keep the data persistence across the whole app.
 */
@Singleton
class VocaPersistenceDatabase @Inject constructor(@ApplicationContext context: Context) : VocaPersistence, CoroutineScope {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VocaPersistenceDatabaseEntryPoint {
        fun vocaDao(): VocaDao
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

//    @Inject
//    lateinit var databaseRoom: RoomVocaDatabase

    private var vocaDao: VocaDao

    private val _allVocabulary = MutableStateFlow<List<Vocabulary?>>(emptyList())

    init {
//        synchronized(this) {
//            databaseRoom = RoomVocaDatabase.getInstance(context)
//        }
        synchronized(this) {
           val hiltEntryPoint = EntryPointAccessors.fromApplication(context, VocaPersistenceDatabaseEntryPoint::class.java)
           vocaDao = hiltEntryPoint.vocaDao()
        }
        loadAllVocabulary()
    }

    override fun getAllVocabulary(): StateFlow<List<Vocabulary?>> {
        return _allVocabulary
    }

//    override suspend fun getAllVocabulary(): List<Vocabulary?> {
//        return allVocabulary.getOrAwaitValue().toVocabularyList()
//    }

    private fun loadAllVocabulary() = launch {
        vocaDao.loadAllVocabulary().collect {
            Logger.d("AllVocabulary loaded!")
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
