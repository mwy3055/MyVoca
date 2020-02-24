package hsk.practice.myvoca.services.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import hsk.practice.myvoca.R;

public class RestartService extends Service {

    private final int RESTART_SERVICE_NOTIFICATION_ID = 10000;

    private final String RESTART_SERVICE_ID = "restart";
    private final String RESTART_SERVICE_NAME = "일반";

    public static final String RESTART_SERVICE = "restart_service";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(RESTART_SERVICE_ID, RESTART_SERVICE_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.drawable.thinking_face)
                .setChannelId(RESTART_SERVICE_ID);

        Notification notification = builder.build();
        startForeground(RESTART_SERVICE_NOTIFICATION_ID, notification);

        Intent restartServiceIntent = new Intent(getApplicationContext(), ShowNotificationService.class);
        restartServiceIntent.putExtra(RESTART_SERVICE, true);
        startService(restartServiceIntent);

        stopForeground(true);
        stopSelf();

        return START_NOT_STICKY;
    }
}
