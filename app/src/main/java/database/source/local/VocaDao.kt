package database.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import database.Vocabulary

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
    fun insertVocabulary(vararg vocabularies: Vocabulary?)

    /**
     * Updates given vocabularies.
     *
     * @param vocabularies objects to update from the database
     */
    @Update
    fun updateVocabulary(vararg vocabularies: Vocabulary?)

    /**
     * Deletes given vocabularies from the database.
     *
     * @param vocabularies objects to delete from the database
     */
    @Delete
    fun deleteVocabulary(vararg vocabularies: Vocabulary?)

    /**
     * Loads all vocabulary and sort the result by ascending alphabetic order.
     * @return all vocabulary
     */
    @Query("SELECT * FROM Vocabulary ORDER BY eng ASC")
    fun loadAllVocabulary(): LiveData<MutableList<Vocabulary?>?>?

    /**
     * Loads vocabularies that matches with the english word
     *
     * @param eng english word to search
     * @return vocabularies whose word is similar to eng
     */
    @Query("SELECT * from Vocabulary WHERE eng LIKE :eng")
    fun loadVocabularyByEng(eng: String?): LiveData<MutableList<Vocabulary?>?>?

    /**
     * Loads vocabularies that matches with the korean meaning
     * @param kor korean meaning to search
     * @return vocabularies whose meaning is similar to kor
     */
    @Query("SELECT * from Vocabulary WHERE kor LIKE :kor")
    fun loadVocabularyByKor(kor: String?): LiveData<MutableList<Vocabulary?>?>?
}