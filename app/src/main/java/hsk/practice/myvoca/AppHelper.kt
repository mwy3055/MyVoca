package hsk.practice.myvoca

import android.Manifest
import android.content.Context
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
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

    const val LOG_TAG = "HSK APP"

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
     * @param timeInMillis Unix epoch timestamp to convert to string
     * @return Time string of the timestamp
     */
    fun getTimeString(timeInMillis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMillis
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
     * @return if application is on the foreground
     */
    fun isForeground(): Boolean {
        return MainActivity.isRunning()
    }

    /**
     * Load database instances.
     *
     * @param context context of the application(mainly SplashActivity)
     */
    fun loadInstance(context: Context) {
        loadLogger()
        VocaPersistenceDatabase.getInstance(context)
    }

    private fun loadLogger() {
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}