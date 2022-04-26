package hsk.practice.myvoca.work

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import hsk.practice.myvoca.room.AndroidTestSampleData
import hsk.practice.myvoca.room.RoomVocaDatabase
import hsk.practice.myvoca.room.todayword.TodayWordDao
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.util.PreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateTodayWordWorkTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var database: RoomVocaDatabase
    private lateinit var vocaDao: VocaDao
    private lateinit var todayWordDao: TodayWordDao
    private lateinit var dataStore: PreferencesDataStore

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, RoomVocaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        vocaDao = database.vocaDao()!!
        todayWordDao = database.todayWordDao()!!
        dataStore = PreferencesDataStore(context)
    }

    @Test
    fun doWork(): Unit = runBlocking {
        val worker = buildWorker()

        val words = AndroidTestSampleData.getSampleRoomVocabularies()
        vocaDao.insertVocabulary(words)

        worker.doWork()

        val todayWords = todayWordDao.getTodayWord().first()
        assert(todayWords.isNotEmpty())
        todayWords.forEach { todayWord ->
            assert(words.any { it.id == todayWord.vocabularyId })
        }
    }

    private fun buildWorker(): CreateTodayWordWorker =
        TestListenableWorkerBuilder<CreateTodayWordWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker {
                    return CreateTodayWordWorker(
                        context,
                        workerParameters,
                        database,
                        dataStore
                    )
                }
            }).build()

}