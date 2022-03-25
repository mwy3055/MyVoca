package com.hsk.domain

import com.hsk.data.vocabulary.Vocabulary
import com.hsk.data.vocabulary.VocabularyQuery
import com.hsk.data.vocabulary.nullVocabulary
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

    suspend fun getAllVocabularyFirstValue(): List<Vocabulary> {
        return getAllVocabulary().first()
    }

    suspend fun getVocabularyById(id: Int): Vocabulary? {
        return vocaPersistence.getVocabularyById(id)
    }

    suspend fun getVocabulary(query: VocabularyQuery): List<Vocabulary> {
        return vocaPersistence.getVocabulary(query)
    }

    suspend fun getRandomVocabulary(): Vocabulary {
        return try {
            val firstAllVocabulary = getAllVocabularyFirstValue()
            firstAllVocabulary.random()
        } catch (e: NoSuchElementException) {
            nullVocabulary
        }
    }

    suspend fun insertVocabulary(vocabularies: List<Vocabulary>) {
        vocaPersistence.insertVocabulary(vocabularies)
    }

    suspend fun updateVocabulary(vocabularies: List<Vocabulary>) {
        vocaPersistence.updateVocabulary(vocabularies)
    }

    suspend fun deleteVocabulary(vocabularies: List<Vocabulary>) {
        vocaPersistence.deleteVocabulary(vocabularies)
    }

}