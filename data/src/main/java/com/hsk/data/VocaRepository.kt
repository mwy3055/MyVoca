package com.hsk.data

import com.hsk.domain.vocabulary.Vocabulary
import com.hsk.domain.vocabulary.nullVocabulary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlin.coroutines.CoroutineContext

class VocaRepository(private val vocaPersistence: VocaPersistence) : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    fun getAllVocabulary(): StateFlow<List<Vocabulary>> {
        return vocaPersistence.getAllVocabulary()
    }

    suspend fun getVocabulary(query: String): List<Vocabulary> {
        return vocaPersistence.getVocabulary(query)
    }

    suspend fun getRandomVocabulary(): Vocabulary {
        return try {
            getAllVocabulary().first().shuffled().first()
        } catch (e: NoSuchElementException) {
            nullVocabulary
        }
    }

    suspend fun insertVocabulary(vararg vocabularies: Vocabulary) {
        vocaPersistence.insertVocabulary(*vocabularies)
    }

    suspend fun updateVocabulary(vararg vocabularies: Vocabulary) {
        vocaPersistence.updateVocabulary(*vocabularies)
    }

    suspend fun deleteVocabulary(vararg vocabularies: Vocabulary) {
        vocaPersistence.deleteVocabulary(*vocabularies)
    }

}