package hsk.practice.myvoca.room.vocabulary

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hsk.data.vocabulary.Meaning
import com.hsk.data.vocabulary.Vocabulary
import com.hsk.data.vocabulary.nullVocabulary
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.toMeaning
import hsk.practice.myvoca.data.toMeaningImpl
import java.lang.reflect.Type

internal inline fun <reified T> getTypeTokenType(): Type = object : TypeToken<T>() {}.type

internal inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, getTypeTokenType<T>())

internal inline fun <reified T> List<T>.toJson(): String {
    val gson = Gson()
    val type = getTypeTokenType<List<T>>()
    return gson.toJson(this, type)
}

/* Convert Vocabulary to other types */
fun Vocabulary.toRoomVocabulary(): RoomVocabulary {
    val meaningString = meaning.toJson()
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
    val meaningList = kor?.let { Gson().fromJson<List<Meaning>>(it) } ?: return nullVocabulary
    return Vocabulary(id, eng, meaningList, addedTime, lastEditedTime, memo)
}

fun RoomVocabulary.toVocabularyImpl(): VocabularyImpl {
    val meaningList =
        kor?.let { Gson().fromJson<List<MeaningImpl>>(it) } ?: return VocabularyImpl.nullVocabulary
    return VocabularyImpl(id, eng, meaningList, addedTime, lastEditedTime, memo)
}

fun List<RoomVocabulary>.toVocabularyList() = this.map { it.toVocabulary() }

fun List<RoomVocabulary>.vocabularyImplList() = this.map { it.toVocabularyImpl() }

fun Array<out RoomVocabulary>.toVocabularyArray() = this.map { it.toVocabulary() }.toTypedArray()


/* Convert VocabularyImpl to other types */
fun VocabularyImpl.toRoomVocabulary(): RoomVocabulary = RoomVocabulary(
    id = id,
    eng = eng,
    kor = meaning.toJson(),
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