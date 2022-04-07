package hsk.practice.myvoca.room.persistence

import com.hsk.data.Meaning
import com.hsk.data.Vocabulary
import com.hsk.data.VocabularyQuery
import com.hsk.data.WordClass
import com.hsk.domain.VocaPersistenceException
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
        val expected = getExampleVoca()
        persistence.insertVocabulary(listOf(expected))

        val actual = persistence.getVocabularyById(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabulariesIsInserted() = testAfterClear {
        val expected = getExampleVocabularies()
        persistence.insertVocabulary(expected)

        val actual = getSecondAllVocabulary()
        assertEquals(expected, actual)
    }

    private suspend fun getSecondAllVocabulary() = persistence.getAllVocabulary().take(2).last()

    @Test
    fun testVocabularySizeIsCorrect() = testAfterClear {
        val vocabularies = getExampleVocabularies()
        persistence.insertVocabulary(vocabularies)

        val expected = vocabularies.size
        val actual = persistence.getVocabularySize().first()
        assertEquals(expected, actual)
    }

    @Test
    fun testTryToFindVocabularyNotExists() = testAfterClear {
        val voca = getExampleVoca()

        val actual = persistence.getVocabularyById(voca.id)
        assertEquals(null, actual)
    }

    @Test
    fun testTryToUpdateVocabularyNotExists() = testAfterClear {
        val voca = getExampleVoca()
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
        val voca = getExampleVoca()
        val expected = listOf(voca)

        persistence.insertVocabulary(expected)

        val query = VocabularyQuery(word = voca.eng)
        val actual = persistence.getVocabulary(query)
        assertEquals(expected, actual)
    }

    @Test
    fun testUpdateVocabulary() = testAfterClear {
        val voca = getExampleVoca()
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
        val voca = getExampleVoca()

        persistence.insertVocabulary(listOf(voca))
        persistence.deleteVocabulary(listOf(voca))

        val query = VocabularyQuery(word = voca.eng)
        val actual = persistence.getVocabulary(query = query)

        val expected = listOf<Vocabulary>()
        assertEquals(expected, actual)
    }

    private fun getExampleVoca(
        id: Int = 3,
        eng: String = "test",
        meaning: List<Meaning> = listOf(Meaning(WordClass.NOUN, "테스트")),
        memo: String = ""
    ) = Vocabulary.create(
        id = id,
        eng = eng,
        meaning = meaning,
        memo = memo
    )

    private fun getExampleVocabularies() = (3..10).map {
        getExampleVoca(id = it)
    }

    override fun clear() = runBlocking {
        persistence.clearVocabulary()
    }

}