package hsk.practice.myvoca.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun setNightMode(value: Boolean) {
    val mode = if (value) {
        AppCompatDelegate.MODE_NIGHT_YES
    } else {
        AppCompatDelegate.MODE_NIGHT_NO
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:ss")

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
    return when (val diff = currentTime - anotherTime) {
        in 0 until 60 -> "${diff}초 전"
        in 60 until 60 * 60 -> "${diff / 60}분 전"
        in 60 * 60 until 60 * 60 * 24 -> "${diff / (60 * 60)}시간 전"
        else -> "${diff / (60 * 60 * 24)}일 전"
    }
}

fun getSecondsLeftOfDay(current: LocalTime = LocalTime.now(ZoneId.systemDefault())): Long {
    return 60 * 60 * 24L - current.toSecondOfDay()
}