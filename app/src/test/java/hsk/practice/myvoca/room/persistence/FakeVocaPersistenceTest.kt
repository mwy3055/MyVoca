package hsk.practice.myvoca.room.persistence

import com.hsk.data.Meaning
import com.hsk.data.Vocabulary
import com.hsk.data.VocabularyQuery
import com.hsk.data.WordClass
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FakeVocaPersistenceTest {

    @Test
    fun testVocabularyIsInserted() = runBlocking {
        val expected = getExampleVoca()
        val persistence = getPersistence()
        persistence.insertVocabulary(listOf(expected))
        val actual = persistence.getVocabularyById(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabularyQuery_notExists() = runBlocking {
        val persistence = getPersistence()
        val expected = listOf<Vocabulary>()
        val actual = persistence.getVocabulary(VocabularyQuery(word = "empty"))
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabularyQuery_insertThenQuery() = runBlocking {
        val voca = getExampleVoca()
        val expected = listOf(voca)

        val persistence = getPersistence()
        persistence.insertVocabulary(expected)

        val query = VocabularyQuery(word = voca.eng)
        val actual = persistence.getVocabulary(query)
        assertEquals(expected, actual)
    }

    @Test
    fun testUpdateVocabulary() = runBlocking {
        val voca = getExampleVoca()

        val persistence = getPersistence()
        persistence.insertVocabulary(listOf(voca))

        val newVoca = voca.copy(eng = "android")
        val expected = listOf(newVoca)
        persistence.updateVocabulary(expected)

        val query = VocabularyQuery(word = newVoca.eng)
        val actual = persistence.getVocabulary(query = query)
        assertEquals(expected, actual)
    }

    @Test
    fun testDeleteVocabulary() = runBlocking {
        val voca = getExampleVoca()

        val persistence = getPersistence()
        persistence.insertVocabulary(listOf(voca))
        persistence.deleteVocabulary(listOf(voca))

        val query = VocabularyQuery(word = voca.eng)
        val actual = persistence.getVocabulary(query = query)

        val expected = listOf<Vocabulary>()
        assertEquals(expected, actual)
    }

    private fun getExampleVoca() = Vocabulary.create(
        id = 3,
        eng = "test",
        meaning = listOf(Meaning(WordClass.NOUN, "테스트")),
        memo = ""
    )

    private fun getPersistence() = FakeVocaPersistence()

}