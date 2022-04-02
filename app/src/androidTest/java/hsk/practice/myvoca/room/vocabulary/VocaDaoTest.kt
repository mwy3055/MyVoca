package hsk.practice.myvoca.room.vocabulary

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.room.RoomVocaDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class VocaDaoTest {

    private lateinit var vocaDao: VocaDao
    private lateinit var database: RoomVocaDatabase

    private val vocaList = fakeData.map { it.toRoomVocabulary() }

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RoomVocaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        vocaDao = database.vocaDao()!!
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGet() = runBlocking {
        val voca = vocaList[0]
        vocaDao.insertVocabulary(voca)
        val insertedVoca = vocaDao.loadVocabularyByEng("dtd")[0]
        assertEquals(voca, insertedVoca)
    }

    @Test
    @Throws(Exception::class)
    fun insertManyAndGet() = runBlocking {
        vocaDao.insertVocabulary(vocaList)
        val allVocabulary = vocaDao.loadAllVocabulary().first().sortedBy { it.id }
        for ((v1, v2) in vocaList.zip(allVocabulary)) {
            assertEquals(v1, v2)
        }
    }

    @Test
    fun checkIfKeyGenerated(): Unit = runBlocking {
        val test = vocaList[0].copy(
            id = 0
        )
        vocaDao.insertVocabulary(test)
        vocaDao.loadVocabularyByEng("test").forEach {
            assertEquals(it.id, 1)
        }
    }

    @Test
    fun checkIdQuery() = runBlocking {
        val voca = vocaList[0]
        vocaDao.insertVocabulary(voca)
        val testVoca = vocaDao.loadVocabularyById(1)
        assertEquals(voca, testVoca)
    }

    @Test
    fun checkMeaningStored() = runBlocking {
        val currentTime = System.currentTimeMillis()
        val testDataImpl: List<VocabularyImpl> = (1..10).map { value ->
            VocabularyImpl(
                id = value,
                eng = "test$value",
                meaning = listOf(MeaningImpl(WordClassImpl.NOUN, "test $value")),
                addedTime = currentTime,
                lastEditedTime = currentTime,
                memo = ""
            )
        }
        testDataImpl.forEach { vocaDao.insertVocabulary(it.toRoomVocabulary()) }
        val storedData: List<VocabularyImpl> =
            vocaDao.loadAllVocabulary().first().sortedBy { it.id }.map { it.toVocabularyImpl() }
        testDataImpl.zip(storedData).forEach { pair ->
            assertEquals(pair.first, pair.second)
        }
    }
}