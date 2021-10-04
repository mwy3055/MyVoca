package com.hsk.domain

import com.hsk.data.vocabulary.Vocabulary
import com.hsk.data.vocabulary.VocabularyQuery
import kotlinx.coroutines.flow.Flow
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
     * Loads the number of rows in database.
     */
    fun getVocabularySize(): Flow<Int>

    /**
     * Loads a vocabulary whose id is equal to the parameter [id].
     */
    suspend fun getVocabularyById(id: Int): Vocabulary?

    /**
     * Loads a vocabulary which matches with the given query.
     * Query can include english word and word class.
     */
    suspend fun getVocabulary(query: VocabularyQuery): List<Vocabulary>

    /**
     * Inserts vocabularies to the database.
     */
    suspend fun insertVocabulary(vocabularies: List<Vocabulary>)

    /**
     * Updates the given vocabulary.
     */
    suspend fun updateVocabulary(vocabularies: List<Vocabulary>)

    /**
     * Deletes the given vocabulary. This operation is permanent.
     */
    suspend fun deleteVocabulary(vocabularies: List<Vocabulary>)

}