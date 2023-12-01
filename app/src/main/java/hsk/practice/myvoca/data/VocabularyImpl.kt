package hsk.practice.myvoca.data

import com.hsk.data.Meaning
import com.hsk.data.WordClass
import com.hsk.ktx.removed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class VocabularyImpl(
    val id: Int = 0,
    val eng: String = "",
    val meaning: ImmutableList<MeaningImpl> = persistentListOf(),
    val addedTime: Long = 0L,
    val lastEditedTime: Long = 0L,
    val memo: String? = "",
) {
    val answerString: String
        get() = "$eng: ${meaning.joinToString("; ") { it.content }}"

    companion object {
        val nullVocabulary = VocabularyImpl(
            id = 0,
            eng = "null",
            meaning = persistentListOf(),
            addedTime = System.currentTimeMillis(),
            lastEditedTime = System.currentTimeMillis(),
            memo = "",
        )
    }
}

val fakeData: List<VocabularyImpl> = (1..20).map { index ->
    val currentTime = System.currentTimeMillis()
    VocabularyImpl(
        id = index,
        eng = "test$index",
        meaning = (0..2).map {
            MeaningImpl(
                if (it % 2 == 0) WordClassImpl.NOUN else WordClassImpl.VERB,
                "테스트$index"
            )
        }.toImmutableList(),
        addedTime = currentTime,
        lastEditedTime = currentTime,
        memo = "",
    )
}

data class MeaningImpl(
    val type: WordClassImpl = WordClassImpl.UNKNOWN,
    val content: String = "",
)

enum class WordClassImpl(val korean: String) {
    NOUN("명사"),
    PRONOUN("대명사"),
    VERB("동사"),
    ADJECTIVE("형용사"),
    ADVERB("부사"),
    PREPOSITION("전치사"),
    CONJUNCTION("접속사"),
    INTERJECTION("감탄사"),
    UNKNOWN("???");

    companion object {
        fun findByKorean(korean: String) = values().find { it.korean == korean }

        fun actualValues(): List<WordClassImpl> = values().removed(UNKNOWN)
    }
}

fun MeaningImpl.toMeaning() = Meaning(
    type = type.toWordClass(),
    content = content,
)

fun Meaning.toMeaningImpl() = MeaningImpl(
    type = type.toWordClassImpl(),
    content = content,
)

fun WordClassImpl.toWordClass() = WordClass.valueOf(name)

fun WordClass.toWordClassImpl() = WordClassImpl.valueOf(name)