package hsk.practice.myvoca.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.orhanobut.logger.Logger
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs

fun String?.containsOnlyAlphabet(): Boolean {
    if (isNullOrEmpty()) return false
    return all { it.isLowerCase() || it.isUpperCase() || it == '%' }
}

fun <T> Array<T>.removed(value: T) = this.filter { it != value }

fun setNightMode(value: Boolean) {
    val mode = if (value) {
        AppCompatDelegate.MODE_NIGHT_YES
    } else {
        AppCompatDelegate.MODE_NIGHT_NO
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

fun <T> Collection<T>.xor(element: T): Collection<T> {
    return if (this.contains(element)) {
        this.minus(element)
    } else {
        this.plus(element)
    }
}

fun <T> Collection<T>.randoms(size: Int): List<T> {
    if (this.size <= size) return this.toList()
    val elements = mutableListOf<T>()
    repeat(size) {
        elements.add(this.random())
    }
    return elements
}

fun <T> Collection<T>.distinctRandoms(size: Int): List<T> {
    val duplicateRemoved = distinct()
    if (duplicateRemoved.size <= size) return duplicateRemoved
    return duplicateRemoved.shuffled().take(size)
}

fun <T> Collection<T>.truncate(size: Int): List<T> {
    if (size < 0) throw IllegalArgumentException("Size must be non-negative")
    return if (this.size <= size) this.toList() else this.take(size)
}

fun Int.gcd(num: Int): Int {
    return if (this == 0) num else (num % this).gcd(this)
}

val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:ss")

fun writeLogToFile(context: Context, filename: String, log: String) {
    val time = LocalDateTime.now()
    val formattedTime = time.format(timeFormatter)
    context.openFileOutput(filename, Context.MODE_PRIVATE + Context.MODE_APPEND).use {
        it.write("$formattedTime: $log\n".toByteArray())
    }
}

fun getTimeDiffString(
    currentTime: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
    anotherTime: Long
): String {
    if (anotherTime == LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC)) return "오래 전"
    return when (val diff = anotherTime - currentTime) {
        in 0 until 60 -> "${diff}초 전"
        in 60 until 60 * 60 -> "${diff / 60}분 전"
        in 60 * 60 until 60 * 60 * 24 -> "${diff / (60 * 60)}시간 전"
        else -> "${diff / (60 * 60 * 24)}일 전"
    }
}

fun getSecondsLeftOfDay(current: LocalTime = LocalTime.now(ZoneId.systemDefault())): Long {
    Logger.d("Current time: $current")
    return 60 * 60 * 24L - current.toSecondOfDay()
}

/**
 * Floating point equal comparison.
 * Uses [IBM formula](https://www.ibm.com/developerworks/java/library/j-jtp0114/#N10255), which is more robust
 * because taking a ratio of 2 numbers **cancels** out the effect of their scale relative to delta.
 */
fun Float.equalsDelta(other: Float) = abs(this / other - 1) < 1e-5

fun <T, R> Iterable<T>.zipForEach(other: Iterable<R>, block: (T, R) -> Unit) {
    this.zip(other).forEach { (thisInstance, otherInstance) -> block(thisInstance, otherInstance) }
}

fun <T, R> Array<out T>.zipForEach(other: Array<out R>, block: (T, R) -> Unit) {
    this.zip(other).forEach { (thisInstance, otherInstance) -> block(thisInstance, otherInstance) }
}