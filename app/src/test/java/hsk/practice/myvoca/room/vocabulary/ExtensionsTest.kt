package hsk.practice.myvoca.room.vocabulary

import com.hsk.data.Meaning
import hsk.practice.myvoca.TestSampleData.getSampleRoomVocabularies
import hsk.practice.myvoca.TestSampleData.getSampleVoca
import hsk.practice.myvoca.TestSampleData.getSampleVocaImpls
import hsk.practice.myvoca.TestSampleData.getSampleVocabularies
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.util.zipForEach
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExtensionsTest {

    @Test
    fun vocabulary_ToVocabularyImpl() {
        val voca = getSampleVoca()
        val vocaImpl = voca.toVocabularyImpl()

        SoftAssertions().apply {
            assertThat(vocaImpl.id).isEqualTo(voca.id)
            assertThat(vocaImpl.eng).isEqualTo(voca.eng)
            assertMeaningsEqual(voca.meaning, vocaImpl.meaning)
            assertThat(vocaImpl.addedTime).isEqualTo(voca.addedTime)
            assertThat(vocaImpl.lastEditedTime).isEqualTo(voca.lastEditedTime)
            assertThat(vocaImpl.memo).isEqualTo(voca.memo)
        }.assertAll()
    }

    private fun assertMeaningsEqual(meanings: List<Meaning>, meaningImpls: List<MeaningImpl>) {
        meanings.zipForEach(meaningImpls) { meaning, meaningImpl ->
            assertMeaningEquals(meaning, meaningImpl)
        }
    }

    private fun assertMeaningEquals(meaning: Meaning, meaningImpl: MeaningImpl) {
        assertThat(meaningImpl.type.ordinal).isEqualTo(meaning.type.ordinal)
        assertThat(meaningImpl.content).isEqualTo(meaning.content)
    }

    @Test
    fun vocabularyList_ToRoomVocabularyMutableList() {
        val vocaList = getSampleVocabularies()
        val roomVocaList = vocaList.toRoomVocabularyMutableList()

        vocaList.zipForEach(roomVocaList) { voca, roomVoca ->
            assertEquals(voca.toRoomVocabulary(), roomVoca)
        }
    }

    @Test
    fun vocabularyList_ToVocabularyImplList() {
        val vocaList = getSampleVocabularies()
        val vocaImplList = vocaList.toVocabularyImplList()

        vocaList.zipForEach(vocaImplList) { voca, vocaImpl ->
            assertEquals(voca.toVocabularyImpl(), vocaImpl)
        }
    }

    @Test
    fun vocabularyArray_ToRoomVocabularyArray() {
        val vocaArray = getSampleVocabularies().toTypedArray()
        val roomVocaArray = vocaArray.toRoomVocabularyArray()

        vocaArray.zipForEach(roomVocaArray) { voca, roomVoca ->
            assertEquals(voca.toRoomVocabulary(), roomVoca)
        }
    }

    @Test
    fun roomVocabularyList_ToVocabularyImplList() {
        val roomVocaArray = getSampleVocabularies().toRoomVocabularyList()
        val vocaImplArray = roomVocaArray.vocabularyImplList()

        roomVocaArray.zipForEach(vocaImplArray) { roomVoca, vocaImpl ->
            assertThat(vocaImpl).isEqualTo(roomVoca.toVocabularyImpl())
        }
    }

    @Test
    fun roomVocabularyArray_ToVocabularyArray() {
        val roomVocaArray = getSampleRoomVocabularies().toTypedArray()
        val vocaArray = roomVocaArray.toVocabularyArray()

        roomVocaArray.zipForEach(vocaArray) { roomVoca, voca ->
            assertThat(voca).isEqualTo(roomVoca.toVocabulary())
        }
    }

    @Test
    fun testVocabularyImplListToRoomVocabularyList() {
        val vocaImplList = getSampleVocaImpls()
        val roomVocaList = vocaImplList.toRoomVocabularyList()

        vocaImplList.zipForEach(roomVocaList) { vocaImpl, roomVoca ->
            assertEquals(vocaImpl.toRoomVocabulary(), roomVoca)
        }
    }
}

