package com.hsk.data

import com.hsk.domain.vocabulary.Vocabulary

class VocaRepository(private val vocaPersistence: VocaPersistence) {

    fun getAllVocabulary(): List<Vocabulary?>? {
        return vocaPersistence.getAllVocabulary()
    }

    fun getVocabulary(query: String): List<Vocabulary?>? {
        return vocaPersistence.getVocabulary(query)
    }

    fun getRandomVocabulary(): Vocabulary? {
        val vocabularyList = getAllVocabulary()
        return try {
            vocabularyList?.random()
        } catch (e: NoSuchElementException) {
            Vocabulary("null", "널", 0, 0, "")
        }
    }

    fun insertVocabulary(vararg vocabularies: Vocabulary?) {
        vocaPersistence.insertVocabulary(*vocabularies)
    }

    fun updateVocabulary(vararg vocabularies: Vocabulary?) {
        vocaPersistence.updateVocabulary(*vocabularies)
    }

    fun deleteVocabulary(vararg vocabularies: Vocabulary?) {
        vocaPersistence.deleteVocabulary(*vocabularies)
    }

}