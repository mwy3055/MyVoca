package hsk.practice.myvoca.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.orhanobut.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.firebase.MyFirestore
import hsk.practice.myvoca.room.RoomVocaDatabase
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.room.vocabulary.toRoomVocabulary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@HiltWorker
class FirestoreDownloadWordsWork @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: RoomVocaDatabase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val userIdKey = "user"
    }

    private val vocaDao: VocaDao
        get() = database.vocaDao()!!

    override suspend fun doWork(): Result {
        val userId = inputData.getString(userIdKey) ?: return Result.failure()

        val dataRef = MyFirestore.backupDataReference(userId)
        val result = dataRef.get().addOnFailureListener { error ->
            Logger.e("Firestore download error: $error")
        }.await()

        val words = result.toObjects(VocabularyImpl::class.java)
        withContext(Dispatchers.IO) {
            val databaseList = words.map { it.toRoomVocabulary() }
            vocaDao.insertVocabulary(databaseList)
        }

        return Result.success()
    }

}

private const val downloadWorkTag = "FirestoreDownloadWork"

fun setFirestoreDownloadWork(
    workManager: WorkManager,
    userId: String
) {
    val data = workDataOf(FirestoreDownloadWordsWork.userIdKey to userId)
    val work = OneTimeWorkRequestBuilder<FirestoreDownloadWordsWork>()
        .addTag(downloadWorkTag)
        .setInputData(data)
        .build()
    workManager.enqueueUniqueWork(downloadWorkTag, ExistingWorkPolicy.KEEP, work)
}