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
class VocaPersistenceDatabase @Inject constructor(@ApplicationContext context: Context) :
    VocaPersistence, CoroutineScope {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VocaPersistenceDatabaseEntryPoint {
        fun vocaDao(): VocaDao
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job

    private var vocaDao: VocaDao

    private val _allVocabulary = MutableStateFlow<List<Vocabulary>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val isLoading: StateFlow<Boolean>
        get() = _isLoading

    init {
        synchronized(this) {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context,
                VocaPersistenceDatabaseEntryPoint::class.java
            )
            vocaDao = hiltEntryPoint.vocaDao()
        }
        loadAllVocabulary()
    }

    override fun getAllVocabulary(): StateFlow<List<Vocabulary>> {
        return _allVocabulary
    }

    override fun getVocabularySize(): Flow<Int> = vocaDao.getVocabularySize().distinctUntilChanged()

    override suspend fun getVocabularyById(id: Int): Vocabulary? {
        return vocaDao.loadVocabularyById(id)?.toVocabulary()
    }

    private fun loadAllVocabulary() = launch(Dispatchers.IO) {
        vocaDao.loadAllVocabulary().collect {
//            Logger.d("AllVocabulary loaded!")
            _allVocabulary.value = it.toVocabularyList()
            _isLoading.value = true
        }
    }

    override suspend fun getVocabulary(query: VocabularyQuery): List<Vocabulary> {
//        Logger.d("Filtering waiting...")
        isLoading.first { it }
//        Logger.d("Filtering start with size ${_allVocabulary.value.size}")
        return _allVocabulary.value.filter { vocabulary ->
            vocabulary.matchesWithQuery(query)
        }
    }

    override suspend fun deleteVocabulary(vocabularies: List<Vocabulary>) {
        vocaDao.deleteVocabulary(vocabularies.toRoomVocabularyList())
    }

    override suspend fun updateVocabulary(vocabularies: List<Vocabulary>) {
        vocaDao.updateVocabulary(vocabularies.toRoomVocabularyList())
    }

    override suspend fun insertVocabulary(vocabularies: List<Vocabulary>) {
        vocaDao.insertVocabulary(vocabularies.toRoomVocabularyList())
    }
}
