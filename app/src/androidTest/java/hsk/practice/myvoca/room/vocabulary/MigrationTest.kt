package hsk.practice.myvoca.room.vocabulary

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import hsk.practice.myvoca.room.RoomMigrations
import hsk.practice.myvoca.room.RoomVocaDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "test_db"
    private val tableName = "RoomVocabulary"

    private val currentTime
        get() = System.currentTimeMillis()

    private val testData = (1..20).mapIndexed { index, i ->
        RoomVocabulary(
            i,
            "test$i",
            "테스트$i",
            currentTime,
            currentTime,
            "메모$i"
        )
    }

    // Helper for creating Room databases and migrations
    @get:Rule
    val migrationHelper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RoomVocaDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    // Helper for creating SQLite database
    lateinit var sqliteTestDbHelper: SQLiteTestDbOpenHelper

    @Before
    fun init() {
        sqliteTestDbHelper =
            SQLiteTestDbOpenHelper(ApplicationProvider.getApplicationContext(), TEST_DB)
        SQLiteDatabaseTestHelper.createTable(sqliteTestDbHelper)
    }

    @After
    fun clearTest() {
        SQLiteDatabaseTestHelper.clearDatabase(sqliteTestDbHelper)
    }

    @Test
    fun migrate2To3(): Unit = runBlocking {
        migrationHelper.createDatabase(TEST_DB, 2).apply {
            testData.forEach { data -> insertVocabulary(data, this) }
            close()
        }

        migrationHelper.runMigrationsAndValidate(TEST_DB, 3, true, RoomMigrations.MIGRATION_2_3)
        getMigratedRoomDatabase(RoomMigrations.MIGRATION_2_3).vocaDao()?.let { dao ->
            val data1 = dao.loadVocabularyByEng("test1")!!.first()!!
            val data2 = dao.loadVocabularyByEng("test2")!!.first()!!
            assertEquals(data1.id + 1, data2.id)
        }
    }

    private fun getMigratedRoomDatabase(vararg migrations: Migration): RoomVocaDatabase {
        val database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RoomVocaDatabase::class.java,
            TEST_DB
        )
            .addMigrations(*migrations)
            .build()
        migrationHelper.closeWhenFinished(database)
        return database
    }

    private fun insertVocabulary(vocabulary: RoomVocabulary, db: SupportSQLiteDatabase) {
        val values = ContentValues()
        with(vocabulary) {
            values.put("eng", eng)
            values.put("kor", kor)
            values.put("add_time", addedTime)
            values.put("last_update", lastEditedTime)
            values.put("memo", memo)
        }
        db.insert(tableName, SQLiteDatabase.CONFLICT_REPLACE, values)
    }

}