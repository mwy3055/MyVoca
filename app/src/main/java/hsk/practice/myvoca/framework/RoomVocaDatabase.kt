package hsk.practice.myvoca.framework

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database class. Exists at the bottom of the database abstraction.
 * Supports database migration, creation, etc.
 * Implemented as Singleton because creating database object is very costly.
 */
@Database(entities = [RoomVocabulary::class], version = 1)
abstract class RoomVocaDatabase : RoomDatabase() {
    abstract fun vocaDao(): VocaDao?

    companion object {
        private const val vocaDatabaseName = "RoomVocabulary"

        @Synchronized
        fun getInstance(context: Context?) = loadInstance(context)

        private fun loadInstance(context: Context?) = Room.databaseBuilder(context!!, RoomVocaDatabase::class.java, vocaDatabaseName).build()
    }


/*
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Vocabulary_new " +
                    "(eng TEXT PRIMARY KEY NOT NULL," +
                    " kor TEXT, " +
                    "add_time INTEGER, " +
                    "last_update INTEGER NOT NULL," +
                    "memo TEXT)");
            database.execSQL("INSERT INTO Vocabulary_new (eng, kor, add_time, last_update, memo)" +
                    "SELECT eng, kor, addedTime, lastEditedTime, memo FROM Vocabulary");
            database.execSQL("DROP TABLE Vocabulary");
            database.execSQL("ALTER TABLE Vocabulary_new RENAME TO Vocabulary");
        }
    };
 */
}