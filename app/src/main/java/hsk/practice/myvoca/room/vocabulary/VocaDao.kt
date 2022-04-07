package hsk.practice.myvoca.room.vocabulary

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO class creates actual SQL query.
 * Room executes `suspend` methods in `Dispatchers.IO` by default.
 */
@Dao
interface VocaDao {
    /**
     * Inserts given vocabularies to the database.
     *
     * @param vocabularies Vocabulary objects which will be inserted
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabularies: List<RoomVocabulary>)

    /**
     * Inserts given vocabulary to the database.
     *
     * @param vocabulary Vocabulary object which will be inserted
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: RoomVocabulary)

    /**
     * Updates given vocabularies.
     *
     * @param vocabularies objects to update from the database
     */
    @Update
    suspend fun updateVocabulary(vocabularies: List<RoomVocabulary>)

    /**
     * Deletes given vocabularies from the database.
     *
     * @param vocabularies objects to delete from the database
     */
    @Delete
    suspend fun deleteVocabulary(vocabularies: List<RoomVocabulary>)

    /**
     * Loads all vocabulary and sort the result by ascending alphabetic order.
     * @return all vocabulary
     */
    @Query("SELECT * FROM RoomVocabulary ORDER BY eng ASC")
    fun loadAllVocabulary(): Flow<List<RoomVocabulary>>

    /**
     * Loads vocabulary by id.
     *
     * @param id id to search
     */
    @Query("SELECT * FROM RoomVocabulary WHERE id LIKE :id")
    suspend fun loadVocabularyById(id: Int): RoomVocabulary?

    /**
     * Loads vocabularies that matches with the english word
     *
     * @param eng english word to search
     * @return vocabularies whose word is similar to eng
     */
    @Query("SELECT * from RoomVocabulary WHERE eng LIKE :eng")
    suspend fun loadVocabularyByEng(eng: String?): List<RoomVocabulary>

    /**
     * Loads vocabularies that matches with the korean meaning
     * @param kor korean meaning to search
     * @return vocabularies whose meaning is similar to kor
     */
    @Query("SELECT * from RoomVocabulary WHERE kor LIKE :kor")
    suspend fun loadVocabularyByKor(kor: String?): List<RoomVocabulary>

    @Query("DELETE from RoomVocabulary")
    suspend fun clearVocabularies()

    /**
     * Loads total number of row in database.
     *
     * @return size of the database
     */
    @Query("SELECT COUNT(*) from RoomVocabulary")
    fun getVocabularySize(): Flow<Int>

    /**
     * Loads ``Today's word`` by referencing ``TodayWords`` table.
     *
     * @return List of ``Today's words``
     */
    @Transaction
    @Query("SELECT * FROM RoomVocabulary WHERE id IN (SELECT vocabulary_id FROM TodayWords)")
    fun getTodayWords(): Flow<List<RoomVocabulary>>

}