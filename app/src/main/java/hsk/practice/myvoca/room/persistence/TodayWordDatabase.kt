package hsk.practice.myvoca.room.persistence

import android.content.Context
import androidx.work.WorkManager
import com.hsk.data.TodayWord
import com.hsk.data.vocabulary.Vocabulary
import com.hsk.domain.TodayWordPersistence
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hsk.practice.myvoca.room.todayword.TodayWordDao
import hsk.practice.myvoca.room.todayword.toRoomTodayWord
import hsk.practice.myvoca.room.todayword.toTodayWord
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.room.vocabulary.toVocabularyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * ``Today's word`` Persistence service by Room.
 * Implemented as [Singleton] to keep the data persistence across the whole app.
 */
@Singleton
class TodayWordDatabase @Inject constructor(@ApplicationContext context: Context) :
    TodayWordPersistence, CoroutineScope {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TodayWordDatabaseEntryPoint {
        fun vocaDao(): VocaDao
        fun todayWordDao(): TodayWordDao
    }

    private val workManager = WorkManager.getInstance(context)

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job

    private lateinit var vocaDao: VocaDao
    private lateinit var todayWordDao: TodayWordDao

    init {
        loadEntryPoint(context)
    }

    private fun loadEntryPoint(context: Context) {
        synchronized(this) {
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                TodayWordDatabaseEntryPoint::class.java
            )
            vocaDao = entryPoint.vocaDao()
            todayWordDao = entryPoint.todayWordDao()
        }
    }

    override fun loadTodayWords(): Flow<List<TodayWord>> =
        todayWordDao.getTodayWord().map { todayWords ->
            todayWords.map { it.toTodayWord() }
        }

    override fun loadActualTodayWords(): Flow<List<Vocabulary>> =
        vocaDao.getTodayWords().distinctUntilChanged().map { it.toVocabularyList() }

    override suspend fun storeTodayWord(todayWord: TodayWord) {
        todayWordDao.insertTodayWord(todayWord.toRoomTodayWord())
    }

    override suspend fun storeTodayWords(todayWords: List<TodayWord>) {
        todayWordDao.insertTodayWord(todayWords.map { it.toRoomTodayWord() })
    }

    override suspend fun updateTodayWord(todayWord: TodayWord) {
        todayWordDao.updateTodayWord(todayWord.toRoomTodayWord())
    }

    override suspend fun deleteTodayWord(todayWord: TodayWord) {
        todayWordDao.deleteTodayWord(todayWord.toRoomTodayWord())
    }

    override suspend fun clearTodayWords() {
        todayWordDao.clearTodayWords()
    }
}