package hsk.practice.myvoca.services.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Calendar;

import database.Vocabulary;
import hsk.practice.myvoca.AppHelper;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.VocaViewModel;
import hsk.practice.myvoca.ui.activity.SplashActivity;

public class ShowNotificationService extends LifecycleService {

    private static boolean isRunning = false;

    private ShowVocaNotificationReceiver receiver;

    private VocaViewModel vocaViewModel;
    private NotificationManager notificationManager;

    private final String VOCA_NOTIFICATION_CHANNEL_ID = "VOCA_NOTIFICATION";
    private final String VOCA_NOTIFICATION_CHANNEL_NAME = "단어 보이기";
    private final String VOCA_NOTIFICATION_ID = "1";
    private final int NOTIFICATION_ID = 20;

    public static final String SHOW_VOCA = "단어";

    private final String SHOW_RANDOM_VOCA = "다른 단어";
    private final String SHOW_RANDOM_VOCA_ACTION_NAME = "action.showvoca.notification";
    private final int SHOW_RANDOM_VOCA_ACTION_ID = 200;

    private final String START_APP_ACTION_NAME = "action.showvoca.startapp";
    private final int START_APP_ACTION_ID = 201;

    private final int RESTART_SERVICE_CODE = 220;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("HSK APP", "ShowNotificationService onCreate()");
        AppHelper.loadInstance(getApplicationContext());
        vocaViewModel = new VocaViewModel();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        receiver = new ShowVocaNotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SHOW_RANDOM_VOCA_ACTION_NAME);
        filter.addAction(START_APP_ACTION_NAME);
        registerReceiver(receiver, filter);

        isRunning = true;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d("HSK APP", "ShowNotificationService onStartCommand()");
        AppHelper.loadInstance(getApplicationContext());
        isRunning = true;
        final LiveData<Boolean> isEmpty = vocaViewModel.isEmpty();
        isEmpty.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (intent != null && intent.getSerializableExtra(SHOW_VOCA) != null) {
                    Vocabulary vocabulary = (Vocabulary) intent.getSerializableExtra(SHOW_VOCA);
                    showWordOnNotification(vocabulary);
                    Log.d("HSK APP", "show voca on notification: " + vocabulary.eng);
                } else if (intent != null && !intent.getBooleanExtra(RestartService.RESTART_SERVICE, false) && !aBoolean) {
                    // only when starting with app and database not empty
                    showRandomWordOnNotification();
                    Log.d("HSK APP", "show random word on notification");
                } else if (intent != null && intent.getBooleanExtra(RestartService.RESTART_SERVICE, false)) {
                    Log.d("HSK APP", "restart notification service");
                    setAlarmTimer();
                }
                isEmpty.removeObserver(this);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.d("HSK APP", "ShowNotificationService onDestroy()");

        final LiveData<Boolean> isEmpty = vocaViewModel.isEmpty();
        isEmpty.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean) {
                    setAlarmTimer();
                }
                isEmpty.removeObserver(this);
            }
        });
        unregisterReceiver(receiver);
    }

    /* set alarm timer to restart this service */
    private void setAlarmTimer() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MILLISECOND, 500);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent sendAlarmPI = PendingIntent.getBroadcast(getApplicationContext(), RESTART_SERVICE_CODE, intent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sendAlarmPI);
    }


    /* methods for showing notification */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(VOCA_NOTIFICATION_CHANNEL_ID, VOCA_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder getBuilder(Vocabulary vocabulary) {
        Intent startAppIntent = new Intent(START_APP_ACTION_NAME);
        PendingIntent startAppPI = PendingIntent.getBroadcast(getApplicationContext(), START_APP_ACTION_ID, startAppIntent, 0);

        Intent showVocaIntent = new Intent(SHOW_RANDOM_VOCA_ACTION_NAME);
        PendingIntent showVocaPI = PendingIntent.getBroadcast(getApplicationContext(), SHOW_RANDOM_VOCA_ACTION_ID, showVocaIntent, 0);
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.thinking_face, SHOW_RANDOM_VOCA, showVocaPI);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), VOCA_NOTIFICATION_ID)
                .setSmallIcon(R.drawable.baseline_bookmark_border_24)
                .setContentTitle(vocabulary.eng)
                .setContentText(vocabulary.kor)
                .setAutoCancel(false)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setChannelId(VOCA_NOTIFICATION_CHANNEL_ID)
                .addAction(action)
                .setContentIntent(startAppPI);

        return builder;
    }

    private void showRandomWordOnNotification() {
        LiveData<Vocabulary> vocabulary = vocaViewModel.getRandomVocabulary();
        showWordOnNotification(vocabulary.getValue());
    }

    private void showWordOnNotification(Vocabulary vocabulary) {
        createNotificationChannel();

        NotificationCompat.Builder builder = getBuilder(vocabulary);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    public static boolean isRunning() {
        return isRunning;
    }

    public class ShowVocaNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("HSK APP", "NotificationReceiver onReceive()");
            if (intent.getAction().equalsIgnoreCase(SHOW_RANDOM_VOCA_ACTION_NAME)) {
                // TODO: wait until vocaViewModel is not null
                Log.d("HSK APP", "Show voca on notification");

                showRandomWordOnNotification();
            } else if (intent.getAction().equalsIgnoreCase(START_APP_ACTION_NAME)
                    && !AppHelper.isForeground(ShowNotificationService.this)) {
                Log.d("HSK APP", "Start app");

                Intent startAppIntent = new Intent(getApplicationContext(), SplashActivity.class);
                startAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(startAppIntent);
            }
        }
    }
}
