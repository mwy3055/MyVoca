package database.source.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import database.Vocabulary;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * DAO class creates actual SQL query.
 */
@Dao
public interface VocaDao {
    /**
     * Inserts given vocabularies to the database.
     *
     * @param vocabularies objects to insert to the database
     */
    @Insert(onConflict = REPLACE)
    public void insertVocabulary(Vocabulary... vocabularies);

    /**
     * Updates given vocabularies.
     *
     * @param vocabularies objects to update from the database
     */
    @Update
    public void updateVocabulary(Vocabulary... vocabularies);

    /**
     * Deletes given vocabularies from the database.
     *
     * @param vocabularies objects to delete from the database
     */
    @Delete
    public void deleteVocabulary(Vocabulary... vocabularies);

    /**
     * Loads all vocabulary and sort the result by ascending alphabetic order.
     * @return all vocabulary
     */
    @Query("SELECT * FROM Vocabulary ORDER BY eng ASC")
    public LiveData<List<Vocabulary>> loadAllVocabulary();

    /**
     * Loads vocabularies that matches with the english word
     *
     * @param eng english word to search
     * @return vocabularies whose word is similar to eng
     */
    @Query("SELECT * from Vocabulary WHERE eng LIKE :eng")
    public LiveData<List<Vocabulary>> loadVocabularyByEng(String eng);

    /**
     * Loads vocabularies that matches with the korean meaning
     * @param kor korean meaning to search
     * @return vocabularies whose meaning is similar to kor
     */
    @Query("SELECT * from Vocabulary WHERE kor LIKE :kor")
    public LiveData<List<Vocabulary>> loadVocabularyByKor(String kor);
}
