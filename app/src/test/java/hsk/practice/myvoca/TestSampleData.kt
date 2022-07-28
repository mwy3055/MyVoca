package hsk.practice.myvoca

import com.hsk.data.Meaning
import com.hsk.data.TodayWord
import com.hsk.data.Vocabulary
import com.hsk.data.WordClass
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.room.todayword.RoomTodayWord
import hsk.practice.myvoca.room.vocabulary.RoomVocabulary
import hsk.practice.myvoca.room.vocabulary.toJson
import kotlinx.collections.immutable.toImmutableList

object TestSampleData {
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

    fun getSampleRoomVoca(
        id: Int = 3,
        eng: String = "test",
        kor: String = listOf(Meaning(WordClass.NOUN, "테스트")).toJson(),
        memo: String = ""
    ): RoomVocabulary {
        val currentTime = System.currentTimeMillis()
        return RoomVocabulary(
            id = id,
            eng = eng,
            kor = kor,
            addedTime = currentTime,
            lastEditedTime = currentTime,
            memo = memo
        )
    }

    fun getSampleRoomVocabularies() = (3..10).map {
        getSampleRoomVoca(id = it)
    }

    fun getSampleVocaImpl(
        id: Int = 3,
        eng: String = "test",
        meaning: List<MeaningImpl> = listOf(MeaningImpl(WordClassImpl.NOUN, "테스트")),
        memo: String = ""
    ): VocabularyImpl {
        val currentTime = System.currentTimeMillis()
        return VocabularyImpl(
            id = id,
            eng = eng,
            meaning = meaning.toImmutableList(),
            addedTime = currentTime,
            lastEditedTime = currentTime,
            memo = memo
        )
    }

    fun getSampleVocaImpls() = (3..10).map {
        getSampleVocaImpl(id = it)
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