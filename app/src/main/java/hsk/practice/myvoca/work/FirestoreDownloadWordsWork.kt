package hsk.practice.myvoca.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.firebase.MyFirestore
import hsk.practice.myvoca.room.RoomVocaDatabase
import hsk.practice.myvoca.room.vocabulary.RoomVocabulary
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.room.vocabulary.toRoomVocabularyList
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
        return try {
            tryDownload()
        } catch (e: Throwable) {
            onDownloadWorkError(e)
        }
    }

    private suspend fun tryDownload(): Result {
        val userId = getUserId()
        val firebaseRef = getFirebaseRef(userId)

        val result = startDownloadAndAwait(firebaseRef) { error ->
            throw Error("Firestore download error: $error")
        }

        val words = convertResultToRoomVocabulary(result)
        insertDownloadedVocabularies(words)

        return Result.success()
    }

    private fun getUserId(): String {
        return inputData.getString(userIdKey)!!
    }

    private fun getFirebaseRef(userId: String): CollectionReference {
        return MyFirestore.backupDataReference(userId)
    }

    private suspend fun startDownloadAndAwait(
        reference: CollectionReference,
        onFailure: (Throwable) -> Unit = {}
    ): QuerySnapshot {
        return reference.get().addOnFailureListener(onFailure).await()
    }

    private fun convertResultToRoomVocabulary(result: QuerySnapshot): List<RoomVocabulary> {
        // Stored to VocabularyImpl type in Firebase
        val resultVocabularyImpl = result.toObjects(VocabularyImpl::class.java)
        return resultVocabularyImpl.toRoomVocabularyList()
    }

    private suspend fun insertDownloadedVocabularies(words: List<RoomVocabulary>) =
        withContext(Dispatchers.IO) {
            vocaDao.insertVocabulary(words)
        }

    private fun onDownloadWorkError(e: Throwable): Result {
        return Result.failure()
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