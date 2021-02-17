package hsk.practice.myvoca

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages SharedPreferences values.
 * Now supports String, boolean, int, long type value.
 * All operations should be done with a proper String key.
 */
object PreferenceManager {
    private const val PREFERENCE_NAME: String = "PREFERENCE"
    const val QUIZ_CORRECT: String = "QUIZ_CORRECT"
    const val QUIZ_WRONG: String = "QUIZ_WRONG"

    // Default values for each type: returned when a given key doesn't exist
    private const val DEFAULT_VALUE_STRING: String = ""
    private const val DEFAULT_VALUE_BOOLEAN = false
    private const val DEFAULT_VALUE_INT = 0
    private const val DEFAULT_VALUE_LONG = 0L
    private fun getPreferences(context: Context?): SharedPreferences {
        return context!!.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun setString(context: Context?, key: String?, value: String?) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun setBoolean(context: Context?, key: String?, value: Boolean) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun setInt(context: Context?, key: String?, value: Int) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun setLong(context: Context?, key: String?, value: Long) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getString(context: Context?, key: String?): String? {
        val prefs = getPreferences(context)
        return prefs.getString(key, DEFAULT_VALUE_STRING)
    }

    fun getBoolean(context: Context?, key: String?): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(key, DEFAULT_VALUE_BOOLEAN)
    }

    fun getInt(context: Context?, key: String?): Int {
        val prefs = getPreferences(context)
        return prefs.getInt(key, DEFAULT_VALUE_INT)
    }

    fun getLong(context: Context?, key: String?): Long {
        val prefs = getPreferences(context)
        return prefs.getLong(key, DEFAULT_VALUE_LONG)
    }

    fun removeKey(context: Context?, key: String?) {
        val prefs = getPreferences(context)
        val edit = prefs.edit()
        edit.remove(key)
        edit.apply()
    }

    // Clear all preference values
    fun clear(context: Context?) {
        val prefs = getPreferences(context)
        val edit = prefs.edit()
        edit.clear()
        edit.apply()
    }
}