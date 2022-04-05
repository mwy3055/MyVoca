package hsk.practice.myvoca.room.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hsk.data.TodayWord
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hsk.practice.myvoca.room.TestAfterClear
import hsk.practice.myvoca.room.todayword.RoomTodayWord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TodayWordPersistenceRoomTest : TestAfterClear {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var persistence: TodayWordPersistenceRoom

    @Before
    fun initTest() {
        hiltRule.inject()
    }

    @Test
    fun testInitialTodayWordIsEmpty() = testAfterClear {
        val actual = persistence.loadTodayWords().first()
        val expected = emptyList<RoomTodayWord>()
        assertEquals(expected, actual)
    }

    @Test
    fun testInsertTodayWord() = testAfterClear {
        val voca = getExampleTodayWord()
        persistence.insertTodayWord(voca)

        val actual = loadTodayWordsFlowFirst()
        val expected = listOf(voca)
        assertEquals(expected, actual)
    }

    @Test
    fun testInsertTodayWords() = testAfterClear {
        val actual = getExampleTodayWords()
        persistence.insertTodayWords(actual)

        val expected = loadTodayWordsFlowFirst()
        assertEquals(expected, actual)
    }

    @Test
    fun testUpdateTodayWords() = testAfterClear {
        val todayWords = getExampleTodayWords()
        persistence.insertTodayWords(todayWords)

        val expected = todayWords.map { todayWord -> todayWord.copy(checked = true) }
        expected.forEach { persistence.updateTodayWord(it) }

        val actual = loadTodayWordsFlowFirst()
        assertEquals(expected, actual)
    }

    @Test
    fun testDeleteTodayWord() = testAfterClear {
        val todayWord = getExampleTodayWord()
        persistence.insertTodayWord(todayWord)

        persistence.deleteTodayWord(todayWord)

        val expected = emptyList<TodayWord>()
        val actual = loadTodayWordsFlowFirst()
        assertEquals(expected, actual)
    }

    override fun clear() = runBlocking {
        persistence.clearTodayWords()
    }

    private suspend fun loadTodayWordsFlowFirst() = persistence.loadTodayWords().first()

    private fun getExampleTodayWord() = getExampleTodayWords()[0]

    private fun getExampleTodayWords() = (3..6).map { index ->
        TodayWord(todayId = index, wordId = index, checked = false)
    }

}