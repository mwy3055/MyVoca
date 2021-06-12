package hsk.practice.myvoca.framework

import com.hsk.domain.vocabulary.Vocabulary

fun Vocabulary.toRoomVocabulary() = RoomVocabulary(
    id, eng, kor, addedTime, lastEditedTime, memo
)

fun RoomVocabulary?.toVocabulary() =
    this?.let { Vocabulary(it.id, it.eng, it.kor, it.addedTime, it.lastEditedTime, it.memo) }

fun List<Vocabulary?>?.toRoomVocabularyList() = this?.map { it?.toRoomVocabulary() }

fun List<Vocabulary?>?.toRoomVocabularyMutableList() = this?.toRoomVocabularyList()?.toMutableList()

fun List<RoomVocabulary?>.toVocabularyList() = this.map { it.toVocabulary() }

fun Array<out Vocabulary?>.toRoomVocabularyArray() =
    this.map { it?.toRoomVocabulary() }.toTypedArray()

fun Array<out RoomVocabulary?>.toVocabularyArray() = this.map { it?.toVocabulary() }.toTypedArray()