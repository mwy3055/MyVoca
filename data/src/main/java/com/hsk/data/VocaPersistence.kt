package com.hsk.data

import com.hsk.domain.vocabulary.Vocabulary
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface which abstracts the vocabulary database operations.
 * All operations should be defined here first, then should be implemented at VocaDatabase
 */
interface VocaPersistence {

    /**
     * Loads all vocabularies from the database and return them as LiveData.
     */
    fun getAllVocabulary(): StateFlow<List<Vocabulary>>

    /**
     * Loads a vocabulary whose id is equal to the parameter [id].
     */
    suspend fun getVocabularyById(id: Int): Vocabulary?

    /**
     * Loads a vocabulary which matches with the given query.
     * Query can be English or Korean.
     * Loads a vocabulary whose eng(former) or kor(latter) contains the query.
     */
    suspend fun getVocabulary(query: String): List<Vocabulary>

    /**
     * Inserts vocabularies to the database.
     */
    suspend fun insertVocabulary(vararg vocabularies: Vocabulary)

    /**
     * Updates the given vocabulary.
     */
    suspend fun updateVocabulary(vararg vocabularies: Vocabulary)

    /**
     * Deletes the given vocabulary. This operation is permanent.
     */
    suspend fun deleteVocabulary(vararg vocabularies: Vocabulary)

}