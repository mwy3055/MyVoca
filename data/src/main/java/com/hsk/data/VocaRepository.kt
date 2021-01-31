package com.hsk.data

import com.hsk.domain.vocabulary.Vocabulary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class VocaRepository(private val vocaPersistence: VocaPersistence) : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    suspend fun getAllVocabulary(): List<Vocabulary?>? {
        return vocaPersistence.getAllVocabulary()
    }

    suspend fun getVocabulary(query: String): List<Vocabulary?>? {
        return vocaPersistence.getVocabulary(query)
    }

    suspend fun getRandomVocabulary(): Vocabulary? {
        val vocabularyList = getAllVocabulary()
        return try {
            vocabularyList?.random()
        } catch (e: NoSuchElementException) {
            Vocabulary("null", "널", 0, 0, "")
        }
    }

    suspend fun insertVocabulary(vararg vocabularies: Vocabulary?) {
        vocaPersistence.insertVocabulary(*vocabularies)
    }

    suspend fun updateVocabulary(vararg vocabularies: Vocabulary?) {
        vocaPersistence.updateVocabulary(*vocabularies)
    }

    suspend fun deleteVocabulary(vararg vocabularies: Vocabulary?) {
        vocaPersistence.deleteVocabulary(*vocabularies)
    }

}