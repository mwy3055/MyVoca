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

fun Vocabulary.containsMeaning(query: String): Boolean {
    return meaning.any { it.content.contains(query) }
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