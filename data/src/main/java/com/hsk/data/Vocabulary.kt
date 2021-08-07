package com.hsk.data.vocabulary

import java.io.Serializable

data class Vocabulary(
    val id: Int,
    val eng: String,
    val meaning: List<Meaning>,
    val addedTime: Long,
    val lastEditedTime: Long,
    val memo: String?
) : Serializable

val nullVocabulary: Vocabulary
    get() = Vocabulary(
        id = 0,
        eng = "null",
        meaning = emptyList(),
        addedTime = System.currentTimeMillis(),
        lastEditedTime = System.currentTimeMillis(),
        memo = ""
    )

fun Vocabulary.matchesWithQuery(query: VocabularyQuery): Boolean {
    return matchesWithQueryString(query.word) and matchesWithWordClass(query.wordClass)
}

fun Vocabulary.matchesWithQueryString(query: String): Boolean {
    return query.isEmpty() or eng.contains(query)
}

fun Vocabulary.matchesWithWordClass(query: Set<WordClass>): Boolean {
    return query.isEmpty() or meaning.any { query.contains(it.type) }
}

data class Meaning(
    val type: WordClass,
    val content: String
)

enum class WordClass {
    NOUN,
    PRONOUN,
    VERB,
    ADJECTIVE,
    ADVERB,
    PREPOSITION,
    CONJUNCTION,
    INTERJECTION,
    UNKNOWN
}

data class VocabularyQuery(
    val word: String = "",
    val wordClass: Set<WordClass> = emptySet()
)