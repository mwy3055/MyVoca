package hsk.practice.myvoca.room.persistence

import com.hsk.data.Vocabulary
import com.hsk.data.VocabularyQuery
import com.hsk.domain.VocaPersistenceException
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hsk.practice.myvoca.room.RoomAndroidTestUtils.getSampleVoca
import hsk.practice.myvoca.room.RoomAndroidTestUtils.getSampleVocabularies
import hsk.practice.myvoca.room.TestAfterClear
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
@HiltAndroidTest
class VocaPersistenceRoomTest : TestAfterClear {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var persistence: VocaPersistenceRoom

    @Before
    fun initTest() {
        hiltRule.inject()
    }

    @Test
    fun testVocabularyIsInserted() = testAfterClear {
        val expected = getSampleVoca()
        persistence.insertVocabulary(listOf(expected))

        val actual = persistence.getVocabularyById(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabulariesIsInserted() = testAfterClear {
        val expected = getSampleVocabularies()
        persistence.insertVocabulary(expected)

        val actual = getSecondAllVocabulary()
        assertEquals(expected, actual)
    }

    private suspend fun getSecondAllVocabulary() = persistence.getAllVocabulary().take(2).last()

    @Test
    fun testVocabularySizeIsCorrect() = testAfterClear {
        val vocabularies = getSampleVocabularies()
        persistence.insertVocabulary(vocabularies)

        val expected = vocabularies.size
        val actual = persistence.getVocabularySize().first()
        assertEquals(expected, actual)
    }

    @Test
    fun testTryToFindVocabularyNotExists() = testAfterClear {
        val voca = getSampleVoca()

        val actual = persistence.getVocabularyById(voca.id)
        assertEquals(null, actual)
    }

    @Test
    fun testTryToUpdateVocabularyNotExists() = testAfterClear {
        val voca = getSampleVoca()
        try {
            persistence.updateVocabulary(listOf(voca))
        } catch (e: VocaPersistenceException) {
            assertEquals("id ${voca.id} doesn't exist", e.message)
        }
    }

    @Test
    fun testVocabularyQuery_notExists() = testAfterClear {
        val expected = listOf<Vocabulary>()
        val actual = persistence.getVocabulary(VocabularyQuery(word = "empty"))
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabularyQuery_insertThenQuery() = testAfterClear {
        val voca = getSampleVoca()
        val expected = listOf(voca)

        persistence.insertVocabulary(expected)

        val query = VocabularyQuery(word = voca.eng)
        val actual = persistence.getVocabulary(query)
        assertEquals(expected, actual)
    }

    @Test
    fun testUpdateVocabulary() = testAfterClear {
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
    fun testDeleteVocabulary() = testAfterClear {
        val voca = getSampleVoca()

        persistence.insertVocabulary(listOf(voca))
        persistence.deleteVocabulary(listOf(voca))

        val query = VocabularyQuery(word = voca.eng)
        val actual = persistence.getVocabulary(query = query)

        val expected = listOf<Vocabulary>()
        assertEquals(expected, actual)
    }

    override fun clear() = runBlocking {
        persistence.clearVocabulary()
    }

}