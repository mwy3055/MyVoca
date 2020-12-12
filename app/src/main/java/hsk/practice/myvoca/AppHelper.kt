package hsk.practice.myvoca;

import android.Manifest;
import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import database.source.VocaRepository;
import database.source.local.VocaDatabase;
import hsk.practice.myvoca.ui.activity.MainActivity;

/**
 * Application helper class. Common methods are defined here.
 * All methods are static.
 */
public class AppHelper {

    private static ArrayList<String> permissionsList;

    /**
     * Returns a permission list of the application.
     *
     * @return permission list of the application
     */
    public static ArrayList<String> getPermissionList() {
        if (permissionsList == null) {
            permissionsList = new ArrayList<>();
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return permissionsList;
    }

    /**
     * Write a log to the file.
     * Not implemented completely, will be used at the future
     *
     * @param text string to write to a file
     */
    public static void writeLog(String text) {
        File logFile = new File("sdcard/MyVoca/log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String timeString = getTimeString(System.currentTimeMillis());
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            bw.append(timeString + ": " + text);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a time-formatted string by the given timestamp.
     *
     * @param timeInMilis Unix epoch timestamp to convert to string
     * @return Time string of the timestamp
     */
    public static String getTimeString(long timeInMilis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMilis);

        int year = cal.get(Calendar.YEAR);
        int mon = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        return String.format("%d.%02d.%02d. %02d:%02d:%02d", year, mon + 1, day, hour, min, sec);
    }

    /**
     * Check if the application is on the foreground.
     *
     * @param context context of the application.
     * @return true if application is on the foreground, else otherwise
     */
    public static boolean isForeground(Context context) {
        return MainActivity.isRunning();
    }

    /**
     * Load database instances.
     *
     * @param context context of the application(mainly SplashActivity)
     */
    public static void loadInstance(Context context) {
        VocaDatabase.loadInstance(context);
        VocaRepository.loadInstance();
    }

    /**
     * Checks if the given string contains only alphabet
     *
     * @param str string to check
     * @return true if string contains only alphabet, false otherwise
     */
    public static boolean isStringOnlyAlphabet(String str) {
        if (str == null || str.equals("")) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!('a' <= c && c <= 'z') && !('A' <= c && c <= 'Z') && c != '%') {
                return false;
            }
        }
        return true;
    }
}
