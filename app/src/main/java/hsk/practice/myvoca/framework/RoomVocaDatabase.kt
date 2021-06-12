package hsk.practice.myvoca.framework

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database class. Exists at the bottom of the database abstraction.
 * Supports database migration, creation, etc.
 * <b>
 *
 * Implemented as Singleton because creating database object is very costly.
 */
@Database(entities = [RoomVocabulary::class], version = 3)
abstract class RoomVocaDatabase : RoomDatabase() {
    abstract fun vocaDao(): VocaDao?

    companion object {
        const val vocaDatabaseName = "Vocabulary"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE Vocabulary_new " +
                            "(eng TEXT PRIMARY KEY NOT NULL," +
                            " kor TEXT, " +
                            "add_time INTEGER NOT NULL, " +
                            "last_update INTEGER NOT NULL," +
                            "memo TEXT)"
                )
                database.execSQL(
                    "INSERT INTO Vocabulary_new (eng, kor, add_time, last_update, memo)" +
                            "SELECT eng, kor, add_time, last_update, memo FROM Vocabulary"
                )
                database.execSQL("DROP TABLE Vocabulary")
                database.execSQL("ALTER TABLE Vocabulary_new RENAME TO Vocabulary")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    // Create new table
                    execSQL(
                        listOf(
                            "CREATE TABLE Vocabulary_3",
                            "(id INTEGER PRIMARY KEY AUTO INCREMENT NOT NULL,",
                            "eng TEXT NOT NULL,",
                            "kor TEXT,",
                            "add_time INTEGER NOT NULL,",
                            "last_update INTEGER NOT NULL,",
                            "memo TEXT)"

                        ).joinToString(" ")
                    )
                    execSQL("INSERT INTO Vocabulary_3 (eng, kor, add_time, last_update, memo)" +
                            "SELECT eng, kor, add_time, last_update, memo FROM Vocabulary")
                    execSQL("DROP TABLE Vocabulary")
                    execSQL("ALTER TABLE Vocabulary_3 RENAME TO Vocabulary")
                }
            }

        }

        val migrations = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

    }
}