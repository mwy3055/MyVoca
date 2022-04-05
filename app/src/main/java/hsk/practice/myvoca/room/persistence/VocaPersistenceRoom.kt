package hsk.practice.myvoca.room.persistence

import android.content.Context
import com.hsk.data.vocabulary.Vocabulary
import com.hsk.data.vocabulary.VocabularyQuery
import com.hsk.data.vocabulary.matchesWithQuery
import com.hsk.domain.VocaPersistence
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.room.vocabulary.toRoomVocabularyList
import hsk.practice.myvoca.room.vocabulary.toVocabulary
import hsk.practice.myvoca.room.vocabulary.toVocabularyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * Vocabulary Persistence Room Database.
 * Implemented as singleton to keep the data persistence across the whole app.
 */
@Singleton
class VocaPersistenceRoom @Inject constructor(@ApplicationContext context: Context) :
    VocaPersistence, CoroutineScope {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VocaPersistenceRoomEntryPoint {
        fun vocaDao(): VocaDao
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job

    private lateinit var vocaDao: VocaDao

    private val _allVocabulary = MutableStateFlow<List<Vocabulary>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val isLoading: StateFlow<Boolean>
        get() = _isLoading

    init {
        synchronized(this) {
            val entryPoint = getVocaPersistenceRoomEntryPoint(context)
            assignDao(entryPoint)
        }
        loadAllVocabulary()
    }

    private fun getVocaPersistenceRoomEntryPoint(context: Context) =
        EntryPointAccessors.fromApplication(context, VocaPersistenceRoomEntryPoint::class.java)

    private fun assignDao(entryPoint: VocaPersistenceRoomEntryPoint) {
        vocaDao = entryPoint.vocaDao()
    }

    override fun getAllVocabulary(): StateFlow<List<Vocabulary>> = _allVocabulary

    override fun getVocabularySize(): Flow<Int> = vocaDao.getVocabularySize().distinctUntilChanged()

    override suspend fun getVocabularyById(id: Int): Vocabulary? {
        return vocaDao.loadVocabularyById(id)?.toVocabulary()
    }

    private fun loadAllVocabulary() = launch(Dispatchers.IO) {
        vocaDao.loadAllVocabulary().collect {
            _allVocabulary.value = it.toVocabularyList()
            _isLoading.value = true
        }
    }

    override suspend fun getVocabulary(query: VocabularyQuery): List<Vocabulary> {
        isLoading.first { it }
        return _allVocabulary.value.filter { vocabulary ->
            vocabulary.matchesWithQuery(query)
        }
    }

    override suspend fun deleteVocabulary(vocabularies: List<Vocabulary>) {
        vocaDao.deleteVocabulary(vocabularies.toRoomVocabularyList())
    }

    override suspend fun clearVocabulary() {
        vocaDao.deleteVocabulary(_allVocabulary.value.toRoomVocabularyList())
    }

    override suspend fun updateVocabulary(vocabularies: List<Vocabulary>) {
        vocaDao.updateVocabulary(vocabularies.toRoomVocabularyList())
    }

    override suspend fun insertVocabulary(vocabularies: List<Vocabulary>) {
        vocaDao.insertVocabulary(vocabularies.toRoomVocabularyList())
    }
}
