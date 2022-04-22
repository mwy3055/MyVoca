package hsk.practice.myvoca.room.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hsk.data.TodayWord
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hsk.practice.myvoca.room.RoomAndroidTestUtils.getSampleTodayWord
import hsk.practice.myvoca.room.RoomAndroidTestUtils.getSampleTodayWords
import hsk.practice.myvoca.room.todayword.RoomTodayWord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TodayWordPersistenceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var persistence: FakeTodayWordPersistence

    @Before
    fun initTest() = runBlocking {
        hiltRule.inject()
        persistence.clearTodayWords()
    }

    @Test
    fun loadTodayWords_EmptyAtFirst() = runTest {
        val actual = persistence.loadTodayWords().first()
        val expected = emptyList<RoomTodayWord>()
        assertEquals(expected, actual)
    }

    @Test
    fun insertTodayWord_NormalCase() = runTest {
        val voca = getSampleTodayWord()
        persistence.insertTodayWord(voca)

        val actual = loadTodayWordsFlowFirst()
        val expected = listOf(voca)
        assertEquals(expected, actual)
    }

    @Test
    fun insertTodayWords_NormalCase() = runTest {
        val actual = getSampleTodayWords()
        persistence.insertTodayWords(actual)

        val expected = loadTodayWordsFlowFirst()
        assertEquals(expected, actual)
    }

    @Test
    fun updateTodayWord_UpdateNotExistingObject() = runTest {
        val todayWord = getSampleTodayWord()

        var exceptionOccur = false
        try {
            persistence.updateTodayWord(todayWord)
        } catch (e: NoSuchElementException) {
            exceptionOccur = true
        }
        assert(exceptionOccur)
    }

    @Test
    fun updateTodayWord_UpdateExistingObjects() = runTest {
        val todayWords = getSampleTodayWords()
        persistence.insertTodayWords(todayWords)

        val expected = todayWords.map { todayWord -> todayWord.copy(checked = true) }
        expected.forEach { persistence.updateTodayWord(it) }

        val actual = loadTodayWordsFlowFirst()
        assertEquals(expected, actual)
    }

    @Test
    fun deleteTodayWord_DeleteExistingObject() = runTest {
        val todayWord = getSampleTodayWord()
        persistence.insertTodayWord(todayWord)

        persistence.deleteTodayWord(todayWord)

        val expected = emptyList<TodayWord>()
        val actual = loadTodayWordsFlowFirst()
        assertEquals(expected, actual)
    }

    @Test
    fun deleteTodayWord_DeleteNotExistingObject() = runTest {
        val todayWord = getSampleTodayWord()

        var exceptionOccur = false
        try {
            persistence.deleteTodayWord(todayWord)
        } catch (e: NoSuchElementException) {
            exceptionOccur = true
        }
        assert(exceptionOccur)
    }

    private suspend fun loadTodayWordsFlowFirst() = persistence.loadTodayWords().first()

}