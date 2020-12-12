package hsk.practice.myvoca.services.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent restartIntent = new Intent(context, RestartService.class);
            context.startForegroundService(restartIntent);
        } else {
            Intent restartIntent = new Intent(context, ShowNotificationService.class);
            context.startService(restartIntent);
        }
    }
}
