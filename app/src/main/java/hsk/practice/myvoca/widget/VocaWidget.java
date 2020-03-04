package hsk.practice.myvoca.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.Callable;

import Database.Vocabulary;
import Database.source.VocaRepository;
import hsk.practice.myvoca.AppHelper;
import hsk.practice.myvoca.R;

public class VocaWidget extends AppWidgetProvider {

    private VocaRepository repository = VocaRepository.getInstance();
    private AppWidgetManager manager;

    private RemoteViews remoteView;

    private AsyncTask<Void, Void, LiveData<Vocabulary>> showVocaTask;
    private Callable myTask;

    private UpdateWidgetReceiver receiver;
    private int UPDATE_WIDGET_ID = 17;
    public static final String UPDATE_WIDGET = "action.updatewidget.update";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("HSK APP", "VocaWidget onReceive(): " + intent.getAction());
        init(context);
        super.onReceive(context, intent);

        if (intent.getAction().equalsIgnoreCase(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            int[] widgetIds;
            if (widgetId != -1) {
                Log.d("HSK APP", "Widget here");
                widgetIds = new int[]{widgetId};
            } else {
                Log.d("HSK APP", "Widget here 2");
                ComponentName componentName = new ComponentName(context, VocaWidget.class);
                widgetIds = manager.getAppWidgetIds(componentName);
            }
            this.onUpdate(context, manager, widgetIds);
            showRandomVocabulary(context, widgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d("HSK APP", "VocaWidget onEnabled()");
        super.onEnabled(context);
        init(context);

        AppHelper.loadInstance(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("HSK APP", "VocaWidget onUpdate()");
        AppHelper.loadInstance(context.getApplicationContext());
        String temp = "";
        for (int id : appWidgetIds) {
            temp += id + " ";
        }
        Log.d("HSK APP", "Widget ids in onUpdate(): " + temp);

        ComponentName componentName = new ComponentName(context, VocaWidget.class);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        setPendingIntent(context, remoteViews, appWidgetIds, componentName);
     /*   Intent intent = new Intent(context, VocaWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_voca_button, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);*/
    }

    private void init(Context context) {
        if (remoteView == null) {
            remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        }
        if (manager == null) {
            manager = AppWidgetManager.getInstance(context);
        }
    }

    private void setPendingIntent(Context context, RemoteViews remoteViews, int[] appWidgetIds, ComponentName componentName) {
        Intent intent = new Intent(context, VocaWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), appWidgetIds[0], intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_voca_button, pendingIntent);

        manager.updateAppWidget(componentName, remoteViews);

        manager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d("HSK APP", "Widget onDeleted()");
        super.onDeleted(context, appWidgetIds);
    }

    private void showRandomVocabulary(final Context context, final int[] widgetIds) {
        final LiveData<Vocabulary> randomVocabulary = VocaRepository.getInstance().getRandomVocabulary();
        randomVocabulary.observeForever(new Observer<Vocabulary>() {
            @Override
            public void onChanged(Vocabulary vocabulary) {
                Log.d("HSK APP", "show: " + vocabulary.eng);
                RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                remoteView.setTextViewText(R.id.widget_eng, vocabulary.eng);
                remoteView.setTextViewText(R.id.widget_kor, vocabulary.kor);
                ComponentName componentName = new ComponentName(context, VocaWidget.class);
                setPendingIntent(context, remoteView, widgetIds, componentName);

                String temp = "";
                for (int id : widgetIds) {
                    temp += id + " ";
                }
                Log.d("HSK APP", "Widget ids in showRandomVoca(): " + temp);
                randomVocabulary.removeObserver(this);
            }
        });
    }

    public class UpdateWidgetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("HSK APP", "WidgetReceiver onReceive()");
            if (intent.getAction().equalsIgnoreCase(UPDATE_WIDGET)) {
                ComponentName widgetName = new ComponentName(context.getPackageName(), VocaWidget.class.getName());
                int[] widgetId = manager.getAppWidgetIds(widgetName);
                showRandomVocabulary(context, widgetId);
            }
        }
    }
}
