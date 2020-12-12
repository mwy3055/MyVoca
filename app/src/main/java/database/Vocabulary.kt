package database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * SQL Entity Object of Vocabulary.
 * Why Serializable? To pass the word, the object should be put into the intent.
 *
 * Fields
 * eng: Primary key. English word.
 * kor: Korean meaning of the word.
 * add_time: Time when the word was added.
 * last_update: Time the word was last modified.
 * memo: Memo for the word.
 */
@Entity
class Vocabulary(eng: String?, kor: String?, addedTime: Int, lastEditedTime: Int, memo: String?) : Serializable {
    @kotlin.jvm.JvmField
    @PrimaryKey
    var eng: String
    @kotlin.jvm.JvmField
    var kor: String?

    @kotlin.jvm.JvmField
    @ColumnInfo(name = "add_time")
    var addedTime: Int

    @kotlin.jvm.JvmField
    @ColumnInfo(name = "last_update")
    var lastEditedTime: Int
    @kotlin.jvm.JvmField
    var memo: String?
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        return if (obj !is Vocabulary) false else eng.contentEquals((obj as Vocabulary?).eng)
    }

    init {
        this.eng = eng
        this.kor = kor
        this.addedTime = addedTime
        this.lastEditedTime = lastEditedTime
        this.memo = memo
    }
}