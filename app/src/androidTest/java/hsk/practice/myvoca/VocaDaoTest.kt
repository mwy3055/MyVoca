package hsk.practice.myvoca

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import hsk.practice.myvoca.framework.RoomVocaDatabase
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.VocaDao
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.take
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
        val currentTime = System.currentTimeMillis()
        val voca = RoomVocabulary("test", "테스트", currentTime, currentTime, "dtd")
        vocaDao.insertVocabulary(voca)
        val insertedVoca = vocaDao.loadVocabularyByEng("test")?.get(0)
        assertEquals(voca, insertedVoca)
    }

    @Test
    @Throws(Exception::class)
    fun insertManyAndGet() = runBlocking {
        val currentTime = System.currentTimeMillis()
        val vocaList = (0..10).map { RoomVocabulary("dtd", "dtd", currentTime, currentTime, "dtd") }
        vocaDao.insertVocabulary(*vocaList.toTypedArray())
        val allVocabulary = vocaDao.loadAllVocabulary().take(1)
        allVocabulary.collectIndexed { _, value ->
            for ((v1, v2) in vocaList.zip(value)) {
                assertEquals(v1, v2)
            }
        }
    }
}