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

public class AppHelper {

    private static ArrayList<String> permissionsList;

    public static ArrayList<String> getPermissionList() {
        if (permissionsList == null) {
            permissionsList = new ArrayList<>();
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return permissionsList;
    }

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

    public static boolean isForeground(Context context) {
        return MainActivity.isRunning();
    }

    public static void loadInstance(Context context) {
        VocaDatabase.loadInstance(context);
        VocaRepository.loadInstance();
    }

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
