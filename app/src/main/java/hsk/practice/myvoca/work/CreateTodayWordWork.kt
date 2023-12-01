package hsk.practice.myvoca.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.hsk.ktx.distinctRandoms
import com.orhanobut.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import hsk.practice.myvoca.room.RoomVocaDatabase
import hsk.practice.myvoca.room.todayword.RoomTodayWord
import hsk.practice.myvoca.room.todayword.TodayWordDao
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.util.MyVocaPreferencesKey
import hsk.practice.myvoca.util.PreferencesDataStore
import hsk.practice.myvoca.util.getSecondsLeftOfDay
import hsk.practice.myvoca.util.writeLogToFile
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@HiltWorker
class CreateTodayWordWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: RoomVocaDatabase,
    private val dataStore: PreferencesDataStore,
) : CoroutineWorker(context, workerParams) {

    private val todayWordSize = 5

    private val vocaDao: VocaDao
        get() = database.vocaDao()!!
    private val todayWordDao: TodayWordDao
        get() = database.todayWordDao()!!

    override suspend fun doWork(): Result {
        return try {
            tryUpdateTodayWords()
        } catch (e: Throwable) {
            onTodayWordError(e)
        }
    }

    private suspend fun tryUpdateTodayWords(): Result {
        clearTodayWords()

        val todayWords = getRandomTodayWords()
        updateTodayWords(todayWords)
        setLastUpdatedTime()

        writeUpdateLog(todayWords)
        return Result.success()
    }

    private suspend fun clearTodayWords() {
        todayWordDao.clearTodayWords()
    }

    private suspend fun getRandomTodayWords(): List<RoomTodayWord> {
        val allVocabulary = vocaDao.loadAllVocabulary().first()
        return allVocabulary
            .distinctRandoms(todayWordSize)
            .map { RoomTodayWord(vocabularyId = it.id, checked = false) }
    }

    private suspend fun updateTodayWords(todayWords: List<RoomTodayWord>) {
        todayWordDao.insertTodayWord(todayWords)
    }

    private fun writeUpdateLog(todayWords: List<RoomTodayWord>) {
        writeLogToFile(
            context = applicationContext,
            filename = "today-word-worker.txt",
            log = "Save Today word - $todayWords",
        )
    }

    private suspend fun setLastUpdatedTime() {
        dataStore.setPreference(
            MyVocaPreferencesKey.todayWordLastUpdatedKey,
            LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
        )
    }

    private fun onTodayWordError(e: Throwable): Result {
        Logger.e(e, "Error while refreshing Today's Word")
        return Result.failure()
    }
}

const val createTodayWordWorkerTag = "create_today_word_work"

fun setPeriodicTodayWordWork(workManager: WorkManager) {
    val secondsLeft = getSecondsLeftOfDay()
    val periodicWork = PeriodicWorkRequestBuilder<CreateTodayWordWorker>(1, TimeUnit.DAYS)
        .addTag(createTodayWordWorkerTag)
        .setInitialDelay(secondsLeft, TimeUnit.SECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork(
        createTodayWordWorkerTag,
        ExistingPeriodicWorkPolicy.REPLACE,
        periodicWork
    )
}

fun setOneTimeTodayWordWork(workManager: WorkManager) {
    val oneTimeWork = OneTimeWorkRequestBuilder<CreateTodayWordWorker>()
        .addTag(createTodayWordWorkerTag)
        .build()
    workManager.enqueueUniqueWork(
        createTodayWordWorkerTag,
        ExistingWorkPolicy.REPLACE,
        oneTimeWork
    )
}