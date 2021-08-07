package com.hsk.domain

import com.hsk.data.TodayWord
import com.hsk.data.vocabulary.Vocabulary
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction of persisting ``Today's word`` data.
 * All platform-dependent databases should be abstracted with this interface.
 */
interface TodayWordPersistence {

    /**
     * Loads all ``Today's word``. This function loads the [TodayWord], not [Vocabulary].
     */
    fun loadTodayWords(): Flow<List<TodayWord>>

    /**
     * Loads actual ``Today's word``. This function loads the [Vocabulary] object.
     */
    fun loadActualTodayWords(): Flow<List<Vocabulary>>

    /**
     * Stores a single ``Today's word``.
     */
    suspend fun storeTodayWord(todayWord: TodayWord)

    /**
     * Stores one or many ``Today's word(s)``.
     */
    suspend fun storeTodayWords(todayWords: List<TodayWord>)

    /**
     * Updates a single [TodayWord].
     */
    suspend fun updateTodayWord(todayWord: TodayWord)

    /**
     * Deletes a single ``Today's word``.
     */
    suspend fun deleteTodayWord(todayWord: TodayWord)

    /**
     * Deletes all ``Today's words``.
     */
    suspend fun clearTodayWords()
}