package hsk.practice.myvoca

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import java.util.*

/**
 * Preferences DataStore object. Delegated by preferenceDataStore().
 * Once this property is initialized, it can be accessed through the whole application.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Checks if the given string contains only alphabet.
 *
 * @return `true` if string contains only alphabet, `false` otherwise
 */
fun String?.containsOnlyAlphabet(): Boolean {
    if (this.isNullOrEmpty()) return false
    this.forEach {
        if (it !in 'a'..'z' && it !in 'A'..'Z' && it != '%') {
            return false
        }
    }
    return true
}


/**
 * Returns a time-formatted string by the given timestamp.
 *
 * @return Time string of the timestamp
 */
fun Long.getTimeString(): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    val year = cal[Calendar.YEAR]
    val mon = cal[Calendar.MONTH]
    val day = cal[Calendar.DAY_OF_MONTH]
    val hour = cal[Calendar.HOUR_OF_DAY]
    val min = cal[Calendar.MINUTE]
    val sec = cal[Calendar.SECOND]
    return String.format("%d.%02d.%02d. %02d:%02d:%02d", year, mon + 1, day, hour, min, sec)
}

/**
 * Sets night mode.
 *
 * @param value whether to enable night mode
 */
fun setNightMode(value: Boolean) {
    val mode = if (value) {
        AppCompatDelegate.MODE_NIGHT_YES
    } else {
        AppCompatDelegate.MODE_NIGHT_NO
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}