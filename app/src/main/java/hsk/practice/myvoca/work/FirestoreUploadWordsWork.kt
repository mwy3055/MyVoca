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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take

@HiltWorker
class FirestoreUploadWordsWork @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: RoomVocaDatabase
) : CoroutineWorker(context, workerParams) {

    private val backupDocument = "backup"
    private val backupData = "data"

    companion object {
        const val userIdKey = "user"
        const val progressKey = "progress"
    }

    private val vocaDao: VocaDao
        get() = database.vocaDao()!!

    override suspend fun doWork(): Result {
        val uid = inputData.getString(userIdKey) ?: return Result.failure()
        vocaDao.loadAllVocabulary().take(2).map { list -> list.vocabularyImplList() }
            .collectLatest { words ->
                val backupRef = MyFirestore.backupDataReference(uid)
                var progress = 0f
                val progressPerWord = 100f / words.size

                words.forEach { word ->
                    backupRef.document(word.id.toString()).set(word)
                        .addOnSuccessListener {
                            Logger.d("Firestore Success!")
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
) {
    val data = workDataOf(FirestoreUploadWordsWork.userIdKey to userId)
    val work = OneTimeWorkRequestBuilder<FirestoreUploadWordsWork>()
        .addTag(uploadWorkTag)
        .setInputData(data)
        .build()
    workManager.enqueueUniqueWork(uploadWorkTag, ExistingWorkPolicy.KEEP, work)
}