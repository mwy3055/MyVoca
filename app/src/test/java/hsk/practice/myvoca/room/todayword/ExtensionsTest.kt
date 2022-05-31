package hsk.practice.myvoca.room.todayword

import hsk.practice.myvoca.TestSampleData.getSampleRoomTodayWord
import hsk.practice.myvoca.TestSampleData.getSampleTodayWord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ExtensionsTest {

    @Test
    fun testTodayWordToRoomTodayWord() {
        val sample = getSampleTodayWord()
        sample.toRoomTodayWord().apply {
            assertThat(this.todayWordId).isEqualTo(sample.todayId)
            assertThat(this.vocabularyId).isEqualTo(sample.wordId)
            assertThat(this.checked).isEqualTo(sample.checked)
        }
    }

    @Test
    fun testRoomTodayWordToTodayWord() {
        val roomSample = getSampleRoomTodayWord()
        roomSample.toTodayWord().apply {
            assertThat(this.todayId).isEqualTo(roomSample.todayWordId)
            assertThat(this.wordId).isEqualTo(roomSample.vocabularyId)
            assertThat(this.checked).isEqualTo(roomSample.checked)
        }
    }

}