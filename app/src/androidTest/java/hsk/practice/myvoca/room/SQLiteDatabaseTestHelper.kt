package hsk.practice.myvoca.room

class SQLiteDatabaseTestHelper {

    companion object {
        fun createTable(helper: SQLiteTestDbOpenHelper) {
            val db = helper.writableDatabase
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS test " +
                        "(eng TEXT PRIMARY KEY NOT NULL," +
                        " kor TEXT, " +
                        "add_time INTEGER NOT NULL, " +
                        "last_update INTEGER NOT NULL," +
                        "memo TEXT)"
            )
        }

        fun clearDatabase(helper: SQLiteTestDbOpenHelper) {
            helper.writableDatabase.apply {
                execSQL("DROP TABLE IF EXISTS test")
                close()
            }
        }

    }
}