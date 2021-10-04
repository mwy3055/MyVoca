package hsk.practice.myvoca.room.todayword

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodayWord(roomTodayWord: RoomTodayWord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodayWord(roomTodayWords: List<RoomTodayWord>)

    @Update
    suspend fun updateTodayWord(todayWord: RoomTodayWord)

    @Query("DELETE FROM TodayWords")
    suspend fun clearTodayWords()

    @Delete
    suspend fun deleteTodayWord(todayWord: RoomTodayWord)

    @Query("SELECT * FROM TodayWords")
    fun getTodayWord(): Flow<List<RoomTodayWord>>

}