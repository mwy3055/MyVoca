package com.hsk.domain

import com.hsk.data.TodayWord
import com.hsk.data.vocabulary.Vocabulary
import kotlinx.coroutines.flow.Flow

interface TodayWordPersistence {

    fun loadTodayWords(): Flow<List<TodayWord>>

    fun loadActualTodayWords(): Flow<List<Vocabulary>>

    suspend fun insertTodayWord(todayWord: TodayWord)

    suspend fun insertTodayWords(todayWords: List<TodayWord>)

    suspend fun updateTodayWord(todayWord: TodayWord)

    suspend fun deleteTodayWord(todayWord: TodayWord)

    suspend fun clearTodayWords()

}