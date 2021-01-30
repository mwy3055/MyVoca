package hsk.practice.myvoca.framework

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * DAO class creates actual SQL query.
 */
@Dao
interface VocaDao {
    /**
     * Inserts given vocabularies to the database.
     *
     * @param vocabularies objects to insert to the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVocabulary(vararg vocabularies: RoomVocabulary?)

    /**
     * Updates given vocabularies.
     *
     * @param vocabularies objects to update from the database
     */
    @Update
    fun updateVocabulary(vararg vocabularies: RoomVocabulary?)

    /**
     * Deletes given vocabularies from the database.
     *
     * @param vocabularies objects to delete from the database
     */
    @Delete
    fun deleteVocabulary(vararg vocabularies: RoomVocabulary?)

    /**
     * Loads all vocabulary and sort the result by ascending alphabetic order.
     * @return all vocabulary
     */
    @Query("SELECT * FROM RoomVocabulary ORDER BY eng ASC")
    fun loadAllVocabulary(): LiveData<List<RoomVocabulary?>?>?

    /**
     * Loads vocabularies that matches with the english word
     *
     * @param eng english word to search
     * @return vocabularies whose word is similar to eng
     */
    @Query("SELECT * from RoomVocabulary WHERE eng LIKE :eng")
    fun loadVocabularyByEng(eng: String?): LiveData<List<RoomVocabulary?>?>?

    /**
     * Loads vocabularies that matches with the korean meaning
     * @param kor korean meaning to search
     * @return vocabularies whose meaning is similar to kor
     */
    @Query("SELECT * from RoomVocabulary WHERE kor LIKE :kor")
    fun loadVocabularyByKor(kor: String?): LiveData<List<RoomVocabulary?>?>?
}