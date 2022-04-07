package hsk.practice.myvoca.room

import com.hsk.data.Meaning
import com.hsk.data.TodayWord
import com.hsk.data.Vocabulary
import com.hsk.data.WordClass
import hsk.practice.myvoca.room.todayword.RoomTodayWord

object RoomTestUtils {
    fun getSampleVoca(
        id: Int = 3,
        eng: String = "test",
        meaning: List<Meaning> = listOf(Meaning(WordClass.NOUN, "테스트")),
        memo: String = ""
    ) = Vocabulary.create(
        id = id,
        eng = eng,
        meaning = meaning,
        memo = memo
    )

    fun getSampleVocabularies() = (3..10).map {
        getSampleVoca(id = it)
    }

    fun getSampleRoomTodayWord() = RoomTodayWord(
        todayWordId = 3,
        vocabularyId = 3,
        checked = false
    )

    fun getSampleTodayWord() = getSampleTodayWords()[0]

    fun getSampleTodayWords() = (3..6).map { index ->
        TodayWord(todayId = index, wordId = index, checked = false)
    }
}