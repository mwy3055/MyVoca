package hsk.practice.myvoca.room.todayword

import hsk.practice.myvoca.TestSampleData.getSampleRoomTodayWord
import hsk.practice.myvoca.TestSampleData.getSampleTodayWord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class ExtensionsTest {

    @Test
    fun testTodayWordToRoomTodayWord() {
        val todayWord = getSampleTodayWord()
        val roomTodayWord = todayWord.toRoomTodayWord()

        assertEquals(roomTodayWord.todayWordId, todayWord.todayId)
        assertEquals(roomTodayWord.vocabularyId, todayWord.wordId)
        assertEquals(roomTodayWord.checked, todayWord.checked)
    }

    @Test
    fun testRoomTodayWordToTodayWord() {
        val roomTodayWord = getSampleRoomTodayWord()
        val todayWord = roomTodayWord.toTodayWord()

        assertEquals(todayWord.todayId, roomTodayWord.todayWordId)
        assertEquals(todayWord.wordId, roomTodayWord.vocabularyId)
        assertEquals(todayWord.checked, roomTodayWord.checked)
    }

}