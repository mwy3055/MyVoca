package hsk.practice.myvoca

import com.hsk.data.VocabularyQuery
import com.hsk.data.WordClass
import com.hsk.data.matchesWithQuery
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.room.vocabulary.toVocabulary
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VocabularyQueryTest {

    private val data = fakeData.map { it.toVocabulary() }

    @Test
    fun emptyQuery() {
        val query = VocabularyQuery()
        val actual = data.filter { it.matchesWithQuery(query) }
        assertThat(actual).isEqualTo(data)
    }

    @Test
    fun wordQuery() {
        val query = VocabularyQuery(word = "3")
        val expected = data.filter { it.eng.contains("3") }
        val actual = data.filter { it.matchesWithQuery(query) }
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun wordClassSingleQuery() {
        val query = VocabularyQuery(wordClass = setOf(WordClass.NOUN))
        assertThat(data[1].matchesWithQuery(query)).isTrue
    }

    @Test
    fun wordClassQuery() {
        val query = VocabularyQuery(wordClass = setOf(WordClass.NOUN))
        val expected = data.filter { it.meaning.any { meaning -> meaning.type == WordClass.NOUN } }
        val actual = data.filter { it.matchesWithQuery(query) }
        assertThat(actual).isEqualTo(expected)
    }
}