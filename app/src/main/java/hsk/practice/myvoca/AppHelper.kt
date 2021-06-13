package hsk.practice.myvoca

import android.Manifest
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
        val timeString = System.currentTimeMillis().getTimeString()
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
     * Check if the application is on the foreground.
     *
     * @return if application is on the foreground
     */
    fun isForeground(): Boolean {
        return MainActivity.isRunning()
    }
}