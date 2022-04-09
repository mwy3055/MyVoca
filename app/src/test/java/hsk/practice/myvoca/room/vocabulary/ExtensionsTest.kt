package hsk.practice.myvoca.room.vocabulary

import com.hsk.data.Meaning
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.room.RoomTestUtils.getSampleRoomVocabularies
import hsk.practice.myvoca.room.RoomTestUtils.getSampleVoca
import hsk.practice.myvoca.room.RoomTestUtils.getSampleVocaImpls
import hsk.practice.myvoca.room.RoomTestUtils.getSampleVocabularies
import hsk.practice.myvoca.util.zipForEach
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtensionsTest {

    @Test
    fun testVocabularyToVocabularyImpl() {
        val voca = getSampleVoca()
        val vocaImpl = voca.toVocabularyImpl()

        assertEquals(voca.id, vocaImpl.id)
        assertEquals(voca.eng, vocaImpl.eng)
        assertMeaningsEqual(voca.meaning, vocaImpl.meaning)
        assertEquals(voca.addedTime, vocaImpl.addedTime)
        assertEquals(voca.lastEditedTime, vocaImpl.lastEditedTime)
        assertEquals(voca.memo, vocaImpl.memo)
    }

    private fun assertMeaningsEqual(meanings: List<Meaning>, meaningImpls: List<MeaningImpl>) {
        meanings.zipForEach(meaningImpls) { meaning, meaningImpl ->
            assertMeaningEquals(meaning, meaningImpl)
        }
    }

    private fun assertMeaningEquals(meaning: Meaning, meaningImpl: MeaningImpl) {
        assertEquals(meaning.type.ordinal, meaningImpl.type.ordinal)
        assertEquals(meaning.content, meaningImpl.content)
    }

    @Test
    fun testVocabularyListToRoomVocabularyMutableList() {
        val vocaList = getSampleVocabularies()
        val roomVocaList = vocaList.toRoomVocabularyMutableList()

        vocaList.zipForEach(roomVocaList) { voca, roomVoca ->
            assertEquals(voca.toRoomVocabulary(), roomVoca)
        }
    }

    @Test
    fun testVocabularyListToVocabularyImplList() {
        val vocaList = getSampleVocabularies()
        val vocaImplList = vocaList.toVocabularyImplList()

        vocaList.zipForEach(vocaImplList) { voca, vocaImpl ->
            assertEquals(voca.toVocabularyImpl(), vocaImpl)
        }
    }

    @Test
    fun testVocabularyArrayToRoomVocabularyArray() {
        val vocaArray = getSampleVocabularies().toTypedArray()
        val roomVocaArray = vocaArray.toRoomVocabularyArray()

        vocaArray.zipForEach(roomVocaArray) { voca, roomVoca ->
            assertEquals(voca.toRoomVocabulary(), roomVoca)
        }
    }

    @Test
    fun testRoomVocabularyListToVocabularyImplList() {
        val roomVocaArray = getSampleVocabularies().toRoomVocabularyList()
        val vocaImplArray = roomVocaArray.vocabularyImplList()

        roomVocaArray.zipForEach(vocaImplArray) { roomVocabulary, vocabularyImpl ->
            assertEquals(roomVocabulary.toVocabularyImpl(), vocabularyImpl)
        }
    }

    @Test
    fun testRoomVocabularyArrayToVocabularyArray() {
        val roomVocaArray = getSampleRoomVocabularies().toTypedArray()
        val vocaArray = roomVocaArray.toVocabularyArray()

        roomVocaArray.zipForEach(vocaArray) { roomVocabulary, vocabulary ->
            assertEquals(roomVocabulary.toVocabulary(), vocabulary)
        }
    }

    @Test
    fun testVocabularyImplListToRoomVocabularyList() {
        val vocaImplList = getSampleVocaImpls()
        val roomVocaList = vocaImplList.toRoomVocabularyList()

        vocaImplList.zipForEach(roomVocaList) { vocabularyImpl, roomVocabulary ->
            assertEquals(vocabularyImpl.toRoomVocabulary(), roomVocabulary)
        }
    }
}

