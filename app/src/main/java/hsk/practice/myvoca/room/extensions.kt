package hsk.practice.myvoca.room

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hsk.data.vocabulary.Meaning
import com.hsk.data.vocabulary.Vocabulary
import com.hsk.data.vocabulary.nullVocabulary
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.toMeaning
import hsk.practice.myvoca.data.toMeaningImpl

fun <T> jsonToList(json: String): List<T> {
    val type = object : TypeToken<List<T>>() {}.type
    val gson = Gson()
    return gson.fromJson(json, type)
}

/* Convert Vocabulary to other types */
fun Vocabulary.toRoomVocabulary(): RoomVocabulary {
    val gson = Gson()
    val meaningString = gson.toJson(meaning)
    return RoomVocabulary(
        id, eng, meaningString, addedTime, lastEditedTime, memo
    )
}

fun Vocabulary.toVocabularyImpl() = VocabularyImpl(
    id, eng, meaning.map { it.toMeaningImpl() }, addedTime, lastEditedTime, memo
)

fun List<Vocabulary>.toRoomVocabularyList() = this.map { it.toRoomVocabulary() }

fun List<Vocabulary>.toRoomVocabularyMutableList() = this.toRoomVocabularyList().toMutableList()

fun List<Vocabulary>.toVocabularyImplList() = this.map { it.toVocabularyImpl() }

fun Array<out Vocabulary>.toRoomVocabularyArray() =
    this.map { it.toRoomVocabulary() }.toTypedArray()


/* Convert RoomVocabulary to other types */
fun RoomVocabulary.toVocabulary(): Vocabulary {
    val meaningList = kor?.let { jsonToList<Meaning>(it) } ?: return nullVocabulary
    return Vocabulary(id, eng, meaningList, addedTime, lastEditedTime, memo)
}

fun RoomVocabulary.toVocabularyImpl(): VocabularyImpl {
    val meaningList =
        kor?.let { jsonToList<MeaningImpl>(it) } ?: return VocabularyImpl.nullVocabulary
    return VocabularyImpl(id, eng, meaningList, addedTime, lastEditedTime, memo)
}

fun List<RoomVocabulary>.toVocabularyList() = this.map { it.toVocabulary() }

fun List<RoomVocabulary>.vocabularyImplList() = this.map { it.toVocabularyImpl() }

fun Array<out RoomVocabulary>.toVocabularyArray() = this.map { it.toVocabulary() }.toTypedArray()


/* Convert VocabularyImpl to other types */
fun VocabularyImpl.toRoomVocabulary(): RoomVocabulary = RoomVocabulary(
    id = id,
    eng = eng,
    kor = Gson().toJson(meaning),
    addedTime = addedTime,
    lastEditedTime = lastEditedTime,
    memo = memo
)

fun VocabularyImpl.toVocabulary(): Vocabulary = Vocabulary(
    id = id,
    eng = eng,
    meaning = meaning.map { it.toMeaning() },
    addedTime = addedTime,
    lastEditedTime = lastEditedTime,
    memo = memo
)