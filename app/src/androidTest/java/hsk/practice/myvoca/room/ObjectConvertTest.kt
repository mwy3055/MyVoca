package hsk.practice.myvoca.room

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.room.vocabulary.*
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ObjectConvertTest {

    @Test
    fun testJsonToMeaningImpl() {
        val expected = listOf(MeaningImpl(WordClassImpl.NOUN, "테스트"))
        val meaningJson = expected.toJson()
        val actual = Gson().fromJson<List<MeaningImpl>>(meaningJson)
        assertEquals(expected, actual)
    }

    @Test
    fun testVocabularyImplToRoom() {
        val currentTime = System.currentTimeMillis()
        val meaningList = listOf(MeaningImpl(WordClassImpl.NOUN, "테스트"))
        val testData = VocabularyImpl(
            id = 1,
            eng = "test",
            meaning = meaningList,
            addedTime = currentTime,
            lastEditedTime = currentTime,
            memo = ""
        )
        val expected = RoomVocabulary(
            id = 1,
            eng = "test",
            kor = Gson().toJson(meaningList),
            addedTime = currentTime,
            lastEditedTime = currentTime,
            memo = ""
        )
        val actual = testData.toRoomVocabulary()
        assertEquals(expected, actual)
    }

    @Test
    fun testRoomVocabularyToImpl() {
        val currentTime = System.currentTimeMillis()
        val meaningList = listOf(MeaningImpl(WordClassImpl.NOUN, "테스트"))
        val testData = RoomVocabulary(
            id = 1,
            eng = "test",
            kor = Gson().toJson(meaningList),
            addedTime = currentTime,
            lastEditedTime = currentTime,
            memo = ""
        )
        val expected = VocabularyImpl(
            id = 1,
            eng = "test",
            meaning = meaningList,
            addedTime = currentTime,
            lastEditedTime = currentTime,
            memo = ""
        )
        val actual = testData.toVocabularyImpl()
        assertEquals(expected, actual)
    }
}