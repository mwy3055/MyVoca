package hsk.practice.myvoca

import com.hsk.data.vocabulary.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VocabularyQueryTest {

    val data = (1..10).map {
        val currentTime = System.currentTimeMillis()
        Vocabulary(
            id = it,
            eng = "test$it",
            meaning = listOf(
                Meaning(
                    if (it % 2 == 0) WordClass.NOUN else WordClass.VERB,
                    "테스트$it"
                )
            ),
            addedTime = currentTime,
            lastEditedTime = currentTime,
            memo = ""
        )
    }

    @Test
    fun emptyQueryTest() {
        val query = VocabularyQuery()
        val queryResult = data.filter { it.matchesWithQuery(query) }
        assertEquals(data, queryResult)
    }

    @Test
    fun wordQueryTest() {
        val query = VocabularyQuery(word = "3")
        val expected = data.filter { it.eng.contains("3") }
        val actual = data.filter { it.matchesWithQuery(query) }
        assertEquals(expected, actual)
    }

    @Test
    fun wordClassSingleQueryTest() {
        val sample = data[1]
        val query = VocabularyQuery(wordClass = setOf(WordClass.NOUN))
        println(sample)
        assert(sample.matchesWithQuery(query))
    }

    @Test
    fun wordClassQueryTest() {
        val query = VocabularyQuery(wordClass = setOf(WordClass.NOUN))
        val expected = data.filter { it.meaning.any { meaning -> meaning.type == WordClass.NOUN } }
        val actual = data.filter { it.matchesWithQuery(query) }
        assertEquals(expected, actual)
    }

}