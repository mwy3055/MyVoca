package hsk.practice.myvoca.ui.screens.addword

import hsk.practice.myvoca.data.MeaningImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class AddWordScreenDataTest {

    private val emptyWord = ""
    private val notEmptyWord = "some word"

    private val emptyMeanings = emptyList<MeaningImpl>()
    private val notEmptyMeanings = listOf(MeaningImpl(content = "content not empty"))

    @Test
    fun canStoreWord_True() {
        assertData(
            word = notEmptyWord,
            wordExistStatus = WordExistStatus.NOT_EXISTS,
            meanings = notEmptyMeanings,
            expected = true
        )
    }

    @Test
    fun canStoreWord_DuplicateWord() {
        assertData(
            word = notEmptyWord,
            wordExistStatus = WordExistStatus.DUPLICATE,
            meanings = notEmptyMeanings,
        )
    }

    @Test
    fun canStoreWord_EmptyWord() {
        assertData(
            word = emptyWord,
            wordExistStatus = WordExistStatus.WORD_EMPTY,
            meanings = emptyMeanings
        )
    }

    @Test
    fun canStoreWord_EmptyWord_WrongArgs() {
        assertData(
            word = emptyWord,
            wordExistStatus = WordExistStatus.NOT_EXISTS,
            meanings = notEmptyMeanings
        )
    }

    @Test
    fun canStoreWord_EmptyMeaning() {
        assertData(
            word = notEmptyWord,
            wordExistStatus = WordExistStatus.NOT_EXISTS,
            meanings = emptyMeanings
        )
    }

    private fun assertData(
        word: String,
        wordExistStatus: WordExistStatus,
        meanings: List<MeaningImpl>,
        expected: Boolean = false
    ) {
        val data = AddWordScreenData(
            word = word,
            wordExistStatus = wordExistStatus,
            meanings = meanings
        )
        assertEquals(expected, data.canStoreWord)
    }

}