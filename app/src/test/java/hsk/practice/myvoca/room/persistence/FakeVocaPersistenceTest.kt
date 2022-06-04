package hsk.practice.myvoca.room.persistence

import com.hsk.data.VocabularyQuery
import com.hsk.domain.VocaPersistence
import com.hsk.domain.VocaPersistenceException
import hsk.practice.myvoca.MainCoroutineExtension
import hsk.practice.myvoca.TestSampleData.getSampleVoca
import hsk.practice.myvoca.TestSampleData.getSampleVocabularies
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension


@OptIn(ExperimentalCoroutinesApi::class)
class FakeVocaPersistenceTest {

    private val dispatcher = StandardTestDispatcher()

    @RegisterExtension
    val coroutineExtension = MainCoroutineExtension(dispatcher)

    private val persistence: VocaPersistence = FakeVocaPersistence()

    @BeforeEach
    fun initTest() = runTest {
        persistence.clearVocabulary()
    }

    @Test
    fun testVocabularyIsInserted() = runTest {
        val samples = getSampleVoca()
        persistence.insertVocabulary(listOf(samples))
        assertThat(persistence.getVocabularyById(samples.id)).isEqualTo(samples)
    }

    @Test
    fun testVocabulariesIsInserted() = runTest {
        val samples = getSampleVocabularies()
        persistence.insertVocabulary(samples)
        assertThat(getFirstAllVocabulary()).isEqualTo(samples)
    }

    @Test
    fun testVocabularySizeIsCorrect() = runTest {
        val samples = getSampleVocabularies()
        persistence.insertVocabulary(samples)

        val actual = persistence.getVocabularySize().first()
        assertThat(actual).isEqualTo(samples.size)
    }

    private suspend fun getFirstAllVocabulary() = persistence.getAllVocabulary().first()

    @Test
    fun testTryToFindVocabularyNotExists() = runTest {
        val voca = getSampleVoca()
        assertThrows<VocaPersistenceException> {
            persistence.getVocabularyById(voca.id)
        }
    }

    @Test
    fun testTryToUpdateVocabularyNotExists() = runTest {
        val voca = getSampleVoca()
        assertThrows<VocaPersistenceException> {
            persistence.updateVocabulary(listOf(voca))
        }
    }

    @Test
    fun testVocabularyQuery_notExists() = runTest {
        val result = persistence.getVocabulary(VocabularyQuery(word = "empty"))
        assertThat(result).isEmpty()
    }

    @Test
    fun testVocabularyQuery_insertThenQuery() = runTest {
        val voca = getSampleVoca()
        persistence.insertVocabulary(listOf(voca))

        val query = VocabularyQuery(word = voca.eng)
        val result = persistence.getVocabulary(query)
        assertThat(result).containsOnly(voca)
    }

    @Test
    fun testUpdateVocabulary() = runTest {
        val voca = getSampleVoca()
        persistence.insertVocabulary(listOf(voca))

        val newVoca = voca.copy(eng = "android")
        persistence.updateVocabulary(listOf(newVoca))

        val result = persistence.getVocabulary(VocabularyQuery(word = newVoca.eng))
        assertThat(result).containsOnly(newVoca)
    }

    @Test
    fun testDeleteVocabulary() = runTest {
        val sample = getSampleVoca()
        persistence.insertVocabulary(listOf(sample))
        persistence.deleteVocabulary(listOf(sample))

        val result = persistence.getVocabulary(VocabularyQuery(word = sample.eng))
        assertThat(result).isEmpty()
    }

}