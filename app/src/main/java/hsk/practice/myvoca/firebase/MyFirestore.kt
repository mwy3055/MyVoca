package hsk.practice.myvoca.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * 데이터 저장 구조: ${uid}(컬렉션) - backup(문서) - data(컬렉션)
 * data 안에 각 단어가 개별 문서로 저장된다.
 *
 * Q. 왜 uid로 먼저 묶었는가?
 * A. 유저별 데이터를 묶고 싶었기 때문.
 */

object MyFirestore {

    private const val backupDocument = "backup"
    private const val backupData = "data"

    fun backupDataReference(userId: String) =
        Firebase.firestore.collection("$userId/$backupDocument/$backupData")

    suspend fun collectionExists(collection: CollectionReference): Boolean {
        val result = collection.get().await()
        return !result.isEmpty
    }

    suspend fun collectionExists(path: String): Boolean {
        return collectionExists(Firebase.firestore.collection(path))
    }

}