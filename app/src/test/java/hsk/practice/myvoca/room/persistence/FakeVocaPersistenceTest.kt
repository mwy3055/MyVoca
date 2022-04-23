package hsk.practice.myvoca.room.persistence

import com.hsk.data.Vocabulary
import com.hsk.data.VocabularyQuery
import com.hsk.domain.VocaPersistence
import com.hsk.domain.VocaPersistenceException
import hsk.practice.myvoca.MainCoroutineRule
import hsk.practice.myvoca.TestSampleData.getSampleVoca
import hsk.practice.myvoca.TestSampleData.getSampleVocabularies
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class FakeVocaPersistenceTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val hiltRule = MainCoroutineRule(dispatcher)

    private val persistence: VocaPersistence = FakeVocaPersistence()

    @Before
    fun initTest() = runTest {
        persistence.clearVocabulary()
    }

    @Test
    fun testVocabularyIsInserted() = runTest {
        val expected = getSampleVoca()
        persistence.insertVocabulary(listOf(expected))
        val actual = persistence.getVocabularyById(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabulariesIsInserted() = runTest {
        val expected = getSampleVocabularies()
        persistence.insertVocabulary(expected)
        val actual = getFirstAllVocabulary()
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabularySizeIsCorrect() = runTest {
        val vocabularies = getSampleVocabularies()
        persistence.insertVocabulary(vocabularies)

        val expected = vocabularies.size
        val actual = persistence.getVocabularySize().first()
        assertEquals(expected, actual)
    }

    private suspend fun getFirstAllVocabulary() = persistence.getAllVocabulary().first()

    @Test
    fun testTryToFindVocabularyNotExists() = runTest {
        val voca = getSampleVoca()
        try {
            persistence.getVocabularyById(voca.id)
        } catch (e: VocaPersistenceException) {
            assertEquals("id 3 doesn't exist", e.message)
        }
    }

    @Test
    fun testTryToUpdateVocabularyNotExists() = runTest {
        val voca = getSampleVoca()
        try {
            persistence.updateVocabulary(listOf(voca))
        } catch (e: VocaPersistenceException) {
            assertEquals("id 3 doesn't exist", e.message)
        }
    }

    @Test
    fun testVocabularyQuery_notExists() = runTest {
        val expected = listOf<Vocabulary>()
        val actual = persistence.getVocabulary(VocabularyQuery(word = "empty"))
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabularyQuery_insertThenQuery() = runTest {
        val voca = getSampleVoca()
        val expected = listOf(voca)

        persistence.insertVocabulary(expected)

        val query = VocabularyQuery(word = voca.eng)
        val actual = persistence.getVocabulary(query)
        assertEquals(expected, actual)
    }

    @Test
    fun testUpdateVocabulary() = runTest {
        val voca = getSampleVoca()
        persistence.insertVocabulary(listOf(voca))

        val newVoca = voca.copy(eng = "android")
        val expected = listOf(newVoca)
        persistence.updateVocabulary(expected)

        val query = VocabularyQuery(word = newVoca.eng)
        val actual = persistence.getVocabulary(query = query)
        assertEquals(expected, actual)
    }

    @Test
    fun testDeleteVocabulary() = runTest {
        val voca = getSampleVoca()

        persistence.insertVocabulary(listOf(voca))
        persistence.deleteVocabulary(listOf(voca))

        val query = VocabularyQuery(word = voca.eng)
        val actual = persistence.getVocabulary(query = query)

        val expected = listOf<Vocabulary>()
        assertEquals(expected, actual)
    }

}