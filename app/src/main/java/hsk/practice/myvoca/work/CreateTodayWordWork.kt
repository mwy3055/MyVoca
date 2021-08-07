package hsk.practice.myvoca.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.orhanobut.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import hsk.practice.myvoca.room.RoomVocaDatabase
import hsk.practice.myvoca.room.todayword.RoomTodayWord
import hsk.practice.myvoca.room.todayword.TodayWordDao
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.ui.screens.home.getSecondsLeft
import hsk.practice.myvoca.util.MyVocaPreferences
import hsk.practice.myvoca.util.PreferencesDataStore
import hsk.practice.myvoca.util.randoms
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
    private val dataStore: PreferencesDataStore
) : CoroutineWorker(context, workerParams) {

    private val todayWordSize = 5

    private val vocaDao: VocaDao
        get() = database.vocaDao()!!
    private val todayWordDao: TodayWordDao
        get() = database.todayWordDao()!!

    override suspend fun doWork(): Result {
        return try {
            todayWordDao.clearTodayWords()
            val allVocabulary = vocaDao.loadAllVocabulary().first()
            val todayWords = allVocabulary.map { it.id }.randoms(todayWordSize)
                .map { RoomTodayWord(vocabularyId = it, checked = false) }
            todayWordDao.insertTodayWord(todayWords)
            writeLogToFile(
                context = applicationContext,
                filename = "today-word-worker.txt",
                log = "Save Today word - $todayWords"
            )
            dataStore.setPreferences(
                MyVocaPreferences.todayWordLastUpdatedKey,
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            )
            Result.success()
        } catch (e: Throwable) {
            Logger.e(e, "Error while refreshing Today's Word")
            Result.failure()
        }
    }
}

const val createTodayWordWorkerTag = "create_today_word_work"

fun setPeriodicTodayWordWork(workManager: WorkManager) {
    val secondsLeft = getSecondsLeft()
    val periodicWork = PeriodicWorkRequestBuilder<CreateTodayWordWorker>(1, TimeUnit.DAYS)
        .addTag(createTodayWordWorkerTag)
        .setInitialDelay(secondsLeft, TimeUnit.SECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork(
        createTodayWordWorkerTag,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWork
    )
}

// TODO: 잘 안되면 workmanager 대신 application에서 처리해 버릴 수도 있음
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