package hsk.practice.myvoca.util

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

/**
 * Remove the given [element] if this [Collection] contains the [element] or add if doesn't.
 * This function is similar to bit operator `XOR`.
 */
fun <T> Collection<T>.xor(element: T): Collection<T> {
    return if (this.contains(element)) {
        this.minus(element)
    } else {
        this.plus(element)
    }
}

fun <T> Collection<T>.randoms(size: Int): List<T> {
    if (this.size <= size) return this.toList()
    val elements = mutableSetOf<T>()
    while (elements.size < size) {
        val elem = this.random()
        elements.add(elem)
    }
    return elements.toList()
}

/**
 * Truncates the given collection to [size]. If size of the given collection is smaller than
 * [size], original collection is returned. Otherwise, first [size] elements are returned.
 *
 * @param size Maximum number of elements to include
 */
fun <T> Collection<T>.truncate(size: Int): List<T> {
    return if (this.size <= size) this.toList() else this.toList().subList(0, size)
}

/**
 * Find the Greatest Common Divisor (GCD).
 *
 * @param num Another integer to find the GCD.
 * @return GCD of this and [num].
 */
fun Int.gcd(num: Int): Int {
    return if (this == 0) num else (num % this).gcd(this)
}

/**
 * Write [log] to the file.
 *
 * @param context [Context] of the caller.
 * @param filename Name of the file which log will be written
 * @param log Content which will be written
 */
fun writeLogToFile(context: Context, filename: String, log: String) {
    val time = LocalDateTime.now()
    val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:ss")
    val formattedTime = time.format(timeFormatter)
    context.openFileOutput(filename, Context.MODE_PRIVATE + Context.MODE_APPEND).use {
        it.write("$formattedTime: $log".toByteArray())
    }
}