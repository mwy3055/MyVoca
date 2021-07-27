package hsk.practice.myvoca.room.todayword

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TodayWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodayWord(todayWord: TodayWord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodayWord(todayWords: List<TodayWord>)

    @Query("DELETE FROM TodayWords")
    suspend fun deleteTodayWords()

    @Query("SELECT * FROM TodayWords")
    suspend fun getTodayWord(): List<TodayWord>

}