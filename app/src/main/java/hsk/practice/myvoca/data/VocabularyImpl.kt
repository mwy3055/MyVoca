package hsk.practice.myvoca.data

import com.hsk.data.vocabulary.Meaning
import com.hsk.data.vocabulary.WordClass
import java.io.Serializable

data class VocabularyImpl(
    val id: Int = 0,
    val eng: String = "",
    val meaning: List<MeaningImpl> = emptyList(),
    val addedTime: Long = 0L,
    val lastEditedTime: Long = 0L,
    val memo: String? = ""
) : Serializable {

    companion object {
        val nullVocabulary: VocabularyImpl
            get() = VocabularyImpl(
                id = 0,
                eng = "null",
                meaning = emptyList(),
                addedTime = System.currentTimeMillis(),
                lastEditedTime = System.currentTimeMillis(),
                memo = ""
            )
    }
}

val fakeData = (1..20).map { index ->
    val currentTime = System.currentTimeMillis()
    VocabularyImpl(
        id = index,
        eng = "test$index",
        meaning = (0..2).map {
            MeaningImpl(
                if (it % 2 == 0) WordClassImpl.NOUN else WordClassImpl.VERB,
                "테스트$index"
            )
        },
        addedTime = currentTime,
        lastEditedTime = currentTime,
        memo = ""
    )
}

val VocabularyImpl.answerString: String
    get() = "$eng: ${meaning.joinToString("; ") { it.content }}"

data class MeaningImpl(
    val type: WordClassImpl = WordClassImpl.UNKNOWN,
    val content: String = ""
) : Serializable

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

        fun actualValues(): List<WordClassImpl> = values().filter { it != UNKNOWN }
    }
}

/**
 * Application object to data layer object
 */
fun MeaningImpl.toMeaning(): Meaning = Meaning(
    type = type.toWordClass(),
    content = content
)

fun WordClassImpl.toWordClass(): WordClass = WordClass.valueOf(name)

/**
 * Data layer object to application object
 */
fun Meaning.toMeaningImpl(): MeaningImpl = MeaningImpl(
    type = type.toWordClassImpl(),
    content = content
)

fun WordClass.toWordClassImpl(): WordClassImpl = WordClassImpl.valueOf(name)