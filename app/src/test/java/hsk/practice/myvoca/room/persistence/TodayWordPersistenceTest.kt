package hsk.practice.myvoca.room.persistence

import com.hsk.domain.TodayWordPersistence
import hsk.practice.myvoca.MainCoroutineExtension
import hsk.practice.myvoca.TestSampleData.getSampleTodayWord
import hsk.practice.myvoca.TestSampleData.getSampleTodayWords
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
class TodayWordPersistenceTest {

    private val dispatcher = StandardTestDispatcher()

    @RegisterExtension
    val coroutineExtension = MainCoroutineExtension(dispatcher)

    // Any subclass of TodayWordPersistence
    private val persistence: TodayWordPersistence = FakeTodayWordPersistence(FakeVocaPersistence())

    @BeforeEach
    fun initTest() = runTest {
        persistence.clearTodayWords()
    }

    @Test
    fun loadTodayWords_EmptyAtFirst() = runTest {
        assertThat(persistence.loadTodayWords().first()).isEmpty()
    }

    @Test
    fun insertTodayWord_NormalCase() = runTest {
        val sample = getSampleTodayWord()
        persistence.insertTodayWord(sample)
        assertThat(getTodayWord()).containsOnly(sample)
    }

    @Test
    fun insertTodayWords_NormalCase() = runTest {
        val samples = getSampleTodayWords()
        persistence.insertTodayWords(samples)
        assertThat(getTodayWord()).isEqualTo(samples)
    }

    @Test
    fun updateTodayWord_UpdateNotExistingObject() = runTest {
        val sample = getSampleTodayWord()
        assertThrows<NoSuchElementException> {
            persistence.updateTodayWord(sample)
        }
    }

    @Test
    fun updateTodayWord_UpdateExistingObjects() = runTest {
        val samples = getSampleTodayWords()
        persistence.insertTodayWords(samples)

        val checkedSamples = samples.map { todayWord -> todayWord.copy(checked = true) }
            .onEach { sample -> persistence.updateTodayWord(sample) }
        assertThat(getTodayWord()).isEqualTo(checkedSamples)
    }

    @Test
    fun deleteTodayWord_DeleteExistingObject() = runTest {
        val sample = getSampleTodayWord()
        persistence.insertTodayWord(sample)
        persistence.deleteTodayWord(sample)

        assertThat(getTodayWord()).isEmpty()
    }

    @Test
    fun deleteTodayWord_DeleteNotExistingObject() = runTest {
        val sample = getSampleTodayWord()
        assertThrows<NoSuchElementException> {
            persistence.deleteTodayWord(sample)
        }
    }

    private suspend fun getTodayWord() = persistence.loadTodayWords().first()

}