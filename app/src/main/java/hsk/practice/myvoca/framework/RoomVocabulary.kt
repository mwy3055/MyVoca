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
data class RoomVocabulary(
    @kotlin.jvm.JvmField @PrimaryKey var id: Int,
    @kotlin.jvm.JvmField var eng: String,
    @kotlin.jvm.JvmField var kor: String?,
    @kotlin.jvm.JvmField @ColumnInfo(name = "add_time") var addedTime: Long,
    @kotlin.jvm.JvmField @ColumnInfo(name = "last_update") var lastEditedTime: Long,
    @kotlin.jvm.JvmField var memo: String?
) : Serializable {

    val answerString: String
        get() = "$eng: ${kor?.replace("\n", " ")}"

    companion object {
        val nullVocabulary = RoomVocabulary(
            -1,
            "null",
            "널입니다.",
            System.currentTimeMillis() / 1000,
            System.currentTimeMillis() / 1000,
            ""
        )
    }

    override fun equals(other: Any?) = id == (other as? RoomVocabulary)?.id ?: false

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + eng.hashCode()
        result = 31 * result + (kor?.hashCode() ?: 0)
        result = 31 * result + addedTime.hashCode()
        result = 31 * result + lastEditedTime.hashCode()
        result = 31 * result + (memo?.hashCode() ?: 0)
        return result
    }

}