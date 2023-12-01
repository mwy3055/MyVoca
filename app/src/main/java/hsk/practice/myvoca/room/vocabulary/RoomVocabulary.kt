package hsk.practice.myvoca.room.vocabulary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class RoomVocabulary(
    @JvmField @PrimaryKey(autoGenerate = true) var id: Int,
    @JvmField var eng: String,
    @JvmField var kor: String?,
    @JvmField @ColumnInfo(name = "add_time") var addedTime: Long,
    @JvmField @ColumnInfo(name = "last_update") var lastEditedTime: Long,
    @JvmField var memo: String?,
) : Serializable {

    companion object {
        val nullVocabulary = RoomVocabulary(
            -1,
            "null",
            "널입니다.",
            System.currentTimeMillis() / 1000,
            System.currentTimeMillis() / 1000,
            "",
        )
    }

    override fun equals(other: Any?) = id == ((other as? RoomVocabulary)?.id ?: false)

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