package hsk.practice.myvoca.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.firebase.firestore.CollectionReference
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.firebase.MyFirestore
import hsk.practice.myvoca.room.RoomVocaDatabase
import hsk.practice.myvoca.room.vocabulary.RoomVocabulary
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
        return try {
            tryUpload()
        } catch (e: Throwable) {
            onUploadWorkError(e)
        }
    }

    private suspend fun tryUpload(): Result {
        collectRealAllVocabulary { wordsList ->
            uploadWords(wordsList)
        }
        return Result.success()
    }

    // First value should always be ignored
    private fun takeAllVocabularyFlow(count: Int) = vocaDao.loadAllVocabulary().take(count)

    private suspend fun collectRealAllVocabulary(action: (List<RoomVocabulary>) -> Unit) {
        takeAllVocabularyFlow(2).collectLatest { list -> action(list) }
    }

    private fun uploadWords(roomWordsList: List<RoomVocabulary>) {
        val words = roomWordsList.vocabularyImplList()

        val userId = getUserId()
        val firebaseRef = getFirebaseRef(userId)

        var progress = 0f
        val progressPerWord = 1f / words.size

        words.forEach { word ->
            uploadWord(
                reference = firebaseRef,
                word = word,
                onSuccess = {
                    progress += progressPerWord
                    setProgressAsync(workDataOf(progressKey to progress))
                },
                onFailure = { throwable ->
                    throw throwable
                }
            )
        }
    }

    private fun getUserId() = inputData.getString(userIdKey)!!

    private fun getFirebaseRef(userId: String): CollectionReference {
        return MyFirestore.backupDataReference(userId)
    }

    private fun uploadWord(
        reference: CollectionReference,
        word: VocabularyImpl,
        onSuccess: (Void) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val wordId = word.id.toString()
        reference.document(wordId)
            .set(word)
            .addOnSuccessListener { onSuccess(it) }
            .addOnFailureListener { onFailure(it) }
    }

    private fun onUploadWorkError(e: Throwable): Result {
        return Result.failure()
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