package hsk.practice.myvoca.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.orhanobut.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import hsk.practice.myvoca.firebase.MyFirestore
import hsk.practice.myvoca.room.RoomVocaDatabase
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.room.vocabulary.vocabularyImplList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import java.util.*

@HiltWorker
class FirestoreUploadWordsWork @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: RoomVocaDatabase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val userIdKey = "user"
        const val progressKey = "progress"
    }

    private val vocaDao: VocaDao
        get() = database.vocaDao()!!

    override suspend fun doWork(): Result {
        val uid = inputData.getString(userIdKey) ?: return Result.failure()
        vocaDao.loadAllVocabulary().take(2).collectLatest { wordsList ->
            val words = wordsList.vocabularyImplList()

            val backupRef = MyFirestore.backupDataReference(uid)
            var progress = 0f
            val progressPerWord = 1f / words.size

            words.forEach { word ->
                backupRef.document(word.id.toString()).set(word)
                    .addOnSuccessListener {
                        progress += progressPerWord
                        setProgressAsync(workDataOf(progressKey to progress))
                    }.addOnFailureListener {
                        Logger.e("Firestore error!")
                    }
            }
        }
        return Result.success()
    }

}

private const val uploadWorkTag = "FirestoreUploadWork"

fun setFirestoreUploadWork(
    workManager: WorkManager,
    userId: String
): UUID {
    val data = workDataOf(FirestoreUploadWordsWork.userIdKey to userId)
    val work = OneTimeWorkRequestBuilder<FirestoreUploadWordsWork>()
        .addTag(uploadWorkTag)
        .setInputData(data)
        .build()
    workManager.enqueueUniqueWork(uploadWorkTag, ExistingWorkPolicy.REPLACE, work)
    return work.id
}