package hsk.practice.myvoca.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.room.todayword.RoomTodayWord
import hsk.practice.myvoca.room.todayword.TodayWordDao
import hsk.practice.myvoca.room.vocabulary.VocaDao
import hsk.practice.myvoca.room.vocabulary.toRoomVocabulary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomTodayWordDaoTest {

    private lateinit var vocaDatabase: RoomVocaDatabase
    private val vocaDao: VocaDao
        get() = vocaDatabase.vocaDao()!!

    private val todayWordDao: TodayWordDao
        get() = vocaDatabase.todayWordDao()!!

    private val data = fakeData.map { it.toRoomVocabulary() }

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        vocaDatabase = Room.inMemoryDatabaseBuilder(context, RoomVocaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        vocaDatabase.close()
    }

    @Test
    fun insertTest() = runBlocking {
        vocaDao.insertVocabulary(data)
        val voca = data[0]
        val todayWord = RoomTodayWord(vocabularyId = voca.id, checked = false)
        todayWordDao.insertTodayWord(todayWord)
        val expected = todayWord.vocabularyId
        val actual = todayWordDao.getTodayWord().first().first().vocabularyId
        assertEquals(expected, actual)
    }

    @Test
    fun insertAndGet() = runBlocking {
        vocaDao.insertVocabulary(data)
        val voca = data[0]
        todayWordDao.insertTodayWord(RoomTodayWord(vocabularyId = voca.id, checked = false))
        val expected = listOf(voca)
        val actual = vocaDao.getTodayWords().first()
        assertEquals(expected, actual)
    }

}