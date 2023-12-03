package hsk.practice.myvoca.room.vocabulary

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hsk.data.Meaning
import com.hsk.data.Vocabulary
import com.hsk.data.nullVocabulary
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.toMeaning
import hsk.practice.myvoca.data.toMeaningImpl
import kotlinx.collections.immutable.toImmutableList
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
    id, eng, meaning.map { it.toMeaningImpl() }.toImmutableList(), addedTime, lastEditedTime, memo
)

@JvmName("toRoomVocabularyListVocabulary")
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
    val meaningList = kor?.let { Gson().fromJson<List<MeaningImpl>>(it) }?.toImmutableList()
        ?: return VocabularyImpl.nullVocabulary
    return VocabularyImpl(id, eng, meaningList, addedTime, lastEditedTime, memo)
}

fun List<RoomVocabulary>.toVocabularyList() = this.map { it.toVocabulary() }

fun List<RoomVocabulary>.vocabularyImplList() = this.map { it.toVocabularyImpl() }

fun Array<out RoomVocabulary>.toVocabularyArray() = this.map { it.toVocabulary() }.toTypedArray()

fun List<VocabularyImpl>.toRoomVocabularyList() = this.map { it.toRoomVocabulary() }

@JvmName("toVocabularyListVocabularyImpl")
fun List<VocabularyImpl>.toVocabularyList() = this.map { it.toVocabulary() }

/* Convert VocabularyImpl to other types */
fun VocabularyImpl.toRoomVocabulary(): RoomVocabulary = RoomVocabulary(
    id = id,
    eng = eng,
    kor = meaning.toJson(),
    addedTime = addedTime,
    lastEditedTime = lastEditedTime,
    memo = memo,
)

fun VocabularyImpl.toVocabulary(): Vocabulary = Vocabulary(
    id = id,
    eng = eng,
    meaning = meaning.map { it.toMeaning() },
    addedTime = addedTime,
    lastEditedTime = lastEditedTime,
    memo = memo,
)