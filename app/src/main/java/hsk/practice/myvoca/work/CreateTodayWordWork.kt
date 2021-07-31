package hsk.practice.myvoca.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.orhanobut.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import hsk.practice.myvoca.randoms
import hsk.practice.myvoca.room.RoomVocaDatabase
import hsk.practice.myvoca.room.todayword.RoomTodayWord
import hsk.practice.myvoca.room.todayword.TodayWordDao
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.ui.screens.home.getSecondsLeft
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
class CreateTodayWordWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val todayWordSize = 5

    @Inject
    lateinit var database: RoomVocaDatabase
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
            Result.success()
        } catch (e: Throwable) {
            Logger.e(e, "Error while refreshing Today's Word")
            Result.failure()
        }
    }
}

val createTodayWordWorkerTag = "create_today_word_work"

fun setPeriodicTodayWordWork(workManager: WorkManager) {
    val secondsLeft = getSecondsLeft()
    val periodicWork = PeriodicWorkRequestBuilder<CreateTodayWordWorker>(1, TimeUnit.DAYS)
        .addTag(createTodayWordWorkerTag)
        .setInitialDelay(secondsLeft, TimeUnit.MILLISECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork(
        createTodayWordWorkerTag,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWork
    )
}

fun setOneTimeTodayWordWork(workManager: WorkManager) {
    val oneTimeWork = OneTimeWorkRequestBuilder<CreateTodayWordWorker>()
        .addTag(createTodayWordWorkerTag)
        .build()
    workManager.enqueueUniqueWork(
        createTodayWordWorkerTag,
        ExistingWorkPolicy.KEEP,
        oneTimeWork
    )
}