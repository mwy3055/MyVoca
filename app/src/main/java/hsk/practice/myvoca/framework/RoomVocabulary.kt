package hsk.practice.myvoca.framework

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * SQL Entity Object of Vocabulary.
 * Serializable? To pass the word, the object should be put into the intent.
 *
 * Fields
 * @eng: Primary key. English word.
 * @kor: Korean meaning of the word.
 * @add_time: Time when the word was added.
 * @last_update: Time the word was last modified.
 * @memo: Memo for the word.
 */
@Entity
class RoomVocabulary(@kotlin.jvm.JvmField @PrimaryKey var eng: String, kor: String?, addedTime: Long, lastEditedTime: Long, memo: String?) : Serializable {

    companion object {
        val nullVocabulary = RoomVocabulary(
                "null",
                "널입니다.",
                System.currentTimeMillis() / 1000,
                System.currentTimeMillis() / 1000,
                "")
    }

    @kotlin.jvm.JvmField
    var kor: String? = kor

    @kotlin.jvm.JvmField
    @ColumnInfo(name = "add_time")
    var addedTime: Long = addedTime

    @kotlin.jvm.JvmField
    @ColumnInfo(name = "last_update")
    var lastEditedTime: Long = lastEditedTime

    @kotlin.jvm.JvmField
    var memo: String? = memo

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return if (other is RoomVocabulary) eng.contentEquals(other.eng) else false
    }

}