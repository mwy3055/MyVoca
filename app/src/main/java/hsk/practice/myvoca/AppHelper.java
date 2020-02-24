package hsk.practice.myvoca;

import android.content.Context;

import Database.source.VocaRepository;
import Database.source.local.VocaDatabase;
import hsk.practice.myvoca.ui.activity.MainActivity;

public class AppHelper {

    public static boolean isForeground(Context context) {
        return MainActivity.isRunning();
    }

    public static void loadInstance(Context context) {
        VocaDatabase.loadInstance(context);
        VocaRepository.loadInstance();
    }
}
