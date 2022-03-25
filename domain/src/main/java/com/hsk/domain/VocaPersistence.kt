package com.hsk.domain

import com.hsk.data.vocabulary.Vocabulary
import com.hsk.data.vocabulary.VocabularyQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VocaPersistence {

    fun getAllVocabulary(): StateFlow<List<Vocabulary>>

    fun getVocabularySize(): Flow<Int>

    suspend fun getVocabularyById(id: Int): Vocabulary?

    suspend fun getVocabulary(query: VocabularyQuery): List<Vocabulary>

    suspend fun insertVocabulary(vocabularies: List<Vocabulary>)

    suspend fun updateVocabulary(vocabularies: List<Vocabulary>)

    suspend fun deleteVocabulary(vocabularies: List<Vocabulary>)

}