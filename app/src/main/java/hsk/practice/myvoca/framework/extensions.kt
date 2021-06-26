package hsk.practice.myvoca.framework

import com.hsk.data.vocabulary.Vocabulary
import hsk.practice.myvoca.VocabularyImpl

/* Convert Vocabulary to other types */
fun Vocabulary.toRoomVocabulary() = RoomVocabulary(
    id, eng, kor, addedTime, lastEditedTime, memo
)

fun Vocabulary.toVocabularyImpl() = VocabularyImpl(
    id, eng, kor, addedTime, lastEditedTime, memo
)

fun List<Vocabulary>.toRoomVocabularyList() = this.map { it.toRoomVocabulary() }

fun List<Vocabulary>.toRoomVocabularyMutableList() = this.toRoomVocabularyList().toMutableList()

fun List<Vocabulary>.toVocabularyImplList() = this.map { it.toVocabularyImpl() }

fun Array<out Vocabulary>.toRoomVocabularyArray() =
    this.map { it.toRoomVocabulary() }.toTypedArray()


/* Convert RoomVocabulary to other types */
fun RoomVocabulary.toVocabulary() =
    this.let { Vocabulary(it.id, it.eng, it.kor, it.addedTime, it.lastEditedTime, it.memo) }

fun RoomVocabulary.toVocabularyImpl() =
    this.let { VocabularyImpl(it.id, it.eng, it.kor, it.addedTime, it.lastEditedTime, it.memo) }

fun List<RoomVocabulary>.toVocabularyList() = this.map { it.toVocabulary() }

fun List<RoomVocabulary>.vocabularyImplList() = this.map { it.toVocabularyImpl() }

fun Array<out RoomVocabulary>.toVocabularyArray() = this.map { it.toVocabulary() }.toTypedArray()


/* Convert VocabularyImpl to other types */
fun VocabularyImpl.toRoomVocabulary(): RoomVocabulary = RoomVocabulary(
    id = id,
    eng = eng,
    kor = kor,
    addedTime = addedTime,
    lastEditedTime = lastEditedTime,
    memo = memo
)

fun VocabularyImpl.toVocabulary(): Vocabulary = Vocabulary(
    id = id,
    eng = eng,
    kor = kor,
    addedTime = addedTime,
    lastEditedTime = lastEditedTime,
    memo = memo
)