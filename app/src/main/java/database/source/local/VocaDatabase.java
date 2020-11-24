package database.source.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import database.Vocabulary;
import database.source.VocaRepository;

/**
 * Room database class. Exists at the bottom of the database abstraction.
 * Supports database migration, creation, etc.
 * Implemented as Singleton because creating database object is very costly.
 */
@Database(entities = Vocabulary.class, version = 1)
public abstract class VocaDatabase extends RoomDatabase {
    public abstract VocaDao vocaDao();

    private static VocaDatabase instance;

    public static synchronized VocaDatabase getInstance() {
        return instance;
    }

    public static void loadInstance(Context context) {
        synchronized (VocaRepository.class) {
            if (instance == null) {
                instance = Room.databaseBuilder(context, VocaDatabase.class, "Vocabulary")
                        //.addMigrations(MIGRATION_1_2)
                        .build();
            }
        }
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