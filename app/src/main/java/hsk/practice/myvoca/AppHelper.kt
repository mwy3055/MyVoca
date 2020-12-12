package hsk.practice.myvoca

import android.Manifest
import android.content.Context
import database.source.VocaRepository
import database.source.local.VocaDatabase
import hsk.practice.myvoca.ui.activity.MainActivity
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

/**
 * Application helper class. Common methods are defined here.
 * All methods are static.
 */
object AppHelper {
    private var permissionsList: ArrayList<String?>? = null

    /**
     * Returns a permission list of the application.
     *
     * @return permission list of the application
     */
    fun getPermissionList(): ArrayList<String?>? {
        if (permissionsList == null) {
            permissionsList = ArrayList()
            permissionsList!!.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsList!!.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return permissionsList
    }

    /**
     * Write a log to the file.
     * Not implemented completely, will be used at the future
     *
     * @param text string to write to a file
     */
    fun writeLog(text: String?) {
        val logFile = File("sdcard/MyVoca/log.txt")
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val timeString = getTimeString(System.currentTimeMillis())
        try {
            val bw = BufferedWriter(FileWriter(logFile, true))
            bw.append("$timeString: $text")
            bw.newLine()
            bw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Returns a time-formatted string by the given timestamp.
     *
     * @param timeInMilis Unix epoch timestamp to convert to string
     * @return Time string of the timestamp
     */
    fun getTimeString(timeInMilis: Long): String? {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMilis
        val year = cal[Calendar.YEAR]
        val mon = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]
        val hour = cal[Calendar.HOUR_OF_DAY]
        val min = cal[Calendar.MINUTE]
        val sec = cal[Calendar.SECOND]
        return String.format("%d.%02d.%02d. %02d:%02d:%02d", year, mon + 1, day, hour, min, sec)
    }

    /**
     * Check if the application is on the foreground.
     *
     * @param context context of the application.
     * @return true if application is on the foreground, else otherwise
     */
    fun isForeground(context: Context?): Boolean {
        return MainActivity.Companion.isRunning()
    }

    /**
     * Load database instances.
     *
     * @param context context of the application(mainly SplashActivity)
     */
    fun loadInstance(context: Context?) {
        VocaDatabase.Companion.loadInstance(context)
        VocaRepository.Companion.loadInstance()
    }

    /**
     * Checks if the given string contains only alphabet
     *
     * @param str string to check
     * @return true if string contains only alphabet, false otherwise
     */
    fun isStringOnlyAlphabet(str: String?): Boolean {
        if (str == null || str == "") {
            return false
        }
        for (c in str.toCharArray()) {
            if (!('a' <= c && c <= 'z') && !('A' <= c && c <= 'Z') && c != '%') {
                return false
            }
        }
        return true
    }
}