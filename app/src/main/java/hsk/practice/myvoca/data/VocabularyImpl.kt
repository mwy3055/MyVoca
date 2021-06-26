package hsk.practice.myvoca.data

import com.hsk.data.vocabulary.Meaning
import com.hsk.data.vocabulary.WordClass
import java.io.Serializable

data class VocabularyImpl(
    val id: Int = 0,
    val eng: String,
    val meaning: List<MeaningImpl>,
    val addedTime: Long,
    val lastEditedTime: Long,
    val memo: String?
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

val VocabularyImpl.answerString: String
    get() = "$eng: ${meaning.joinToString("; ") { it.content }}"

data class MeaningImpl(
    val type: WordClassImpl,
    val content: String
) : Serializable

enum class WordClassImpl {
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