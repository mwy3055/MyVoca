package Database.source.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import Database.Vocabulary;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface VocaDao {
    @Insert(onConflict = REPLACE)
    public void insertVocabulary(Vocabulary... vocabularies);

    @Update
    public void updateVocabulary(Vocabulary... vocabularies);

    @Delete
    public void deleteVocabulary(Vocabulary... vocabularies);

    @Query("SELECT * FROM Vocabulary ORDER BY eng ASC")
    public LiveData<List<Vocabulary>> loadAllVocabulary(); // 모든 단어의 영어, 뜻만 load

    @Query("SELECT * from Vocabulary WHERE eng LIKE :eng")
    public LiveData<List<Vocabulary>> loadVocabulary(String eng); // 그 단어의 모든 정보 load
}
