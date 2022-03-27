package hsk.practice.myvoca.room.todayword

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TodayWords")
data class RoomTodayWord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "today_id")
    val todayWordId: Int = 0,
    @ColumnInfo(name = "vocabulary_id")
    val vocabularyId: Int,
    val checked: Boolean
)
