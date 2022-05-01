package hsk.practice.myvoca.room.vocabulary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteTestDbOpenHelper(context: Context, databaseName: String) :
    SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    companion object {
        val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE test_db " +
                    "(eng TEXT PRIMARY KEY NOT NULL," +
                    " kor TEXT, " +
                    "add_time INTEGER NOT NULL, " +
                    "last_update INTEGER NOT NULL," +
                    "memo TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}