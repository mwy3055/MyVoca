package hsk.practice.myvoca

import android.Manifest
import androidx.appcompat.app.AppCompatDelegate
import java.util.*


/**
 * Runtime permissions required for this app.
 */
val permissionsList: List<String> = listOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

/**
 * Checks if the given string contains only alphabet.
 *
 * @return `true` if string contains only alphabet, `false` otherwise
 */
fun String?.containsOnlyAlphabet(): Boolean {
    if (isNullOrEmpty()) return false
    return all { it in 'a'..'z' || it in 'A'..'Z' || it == '%' }
}


/**
 * Returns a time-formatted string by the given timestamp.
 *
 * @return Time string of the timestamp
 */
fun Long.toTimeString(): String {
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