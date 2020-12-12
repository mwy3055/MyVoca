package hsk.practice.myvoca.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.os.AsyncTask
import android.util.Log
import android.widget.RemoteViews
import androidx.lifecycle.*
import database.Vocabulary
import database.source.VocaRepository
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.R
import java.util.concurrent.Callable

class VocaWidget : AppWidgetProvider() {
    private val repository: VocaRepository? = VocaRepository.Companion.getInstance()
    private var manager: AppWidgetManager? = null
    private var remoteView: RemoteViews? = null
    private val showVocaTask: AsyncTask<Void?, Void?, LiveData<Vocabulary?>?>? = null
    private val myTask: Callable<*>? = null
    private val receiver: UpdateWidgetReceiver? = null
    private val UPDATE_WIDGET_ID = 17
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("HSK APP", "VocaWidget onReceive(): " + intent.getAction())
        init(context)
        super.onReceive(context, intent)
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE, ignoreCase = true)) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            val widgetIds: IntArray?
            widgetIds = if (widgetId != -1) {
                Log.d("HSK APP", "Widget here")
                intArrayOf(widgetId)
            } else {
                Log.d("HSK APP", "Widget here 2")
                val componentName = ComponentName(context, VocaWidget::class.java)
                manager.getAppWidgetIds(componentName)
            }
            onUpdate(context, manager, widgetIds)
            showRandomVocabulary(context, widgetIds)
        }
    }

    override fun onEnabled(context: Context?) {
        Log.d("HSK APP", "VocaWidget onEnabled()")
        super.onEnabled(context)
        init(context)
        AppHelper.loadInstance(context)
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        Log.d("HSK APP", "VocaWidget onUpdate()")
        AppHelper.loadInstance(context.getApplicationContext())
        var temp = ""
        for (id in appWidgetIds) {
            temp += "$id "
        }
        Log.d("HSK APP", "Widget ids in onUpdate(): $temp")
        val componentName = ComponentName(context, VocaWidget::class.java)
        val remoteViews = RemoteViews(context.getPackageName(), R.layout.widget_layout)
        setPendingIntent(context, remoteViews, appWidgetIds, componentName)
        /*   Intent intent = new Intent(context, VocaWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_voca_button, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);*/
    }

    private fun init(context: Context?) {
        if (remoteView == null) {
            remoteView = RemoteViews(context.getPackageName(), R.layout.widget_layout)
        }
        if (manager == null) {
            manager = AppWidgetManager.getInstance(context)
        }
    }

    private fun setPendingIntent(context: Context?, remoteViews: RemoteViews?, appWidgetIds: IntArray?, componentName: ComponentName?) {
        val intent = Intent(context, VocaWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds.get(0))
        val pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), appWidgetIds.get(0), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_voca_button, pendingIntent)
        manager.updateAppWidget(componentName, remoteViews)
        manager.updateAppWidget(appWidgetIds, remoteViews)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        Log.d("HSK APP", "Widget onDeleted()")
        super.onDeleted(context, appWidgetIds)
    }

    private fun showRandomVocabulary(context: Context?, widgetIds: IntArray?) {
        val randomVocabulary: LiveData<Vocabulary?> = VocaRepository.Companion.getInstance().getRandomVocabulary()
        randomVocabulary.observeForever(object : Observer<Vocabulary?> {
            override fun onChanged(vocabulary: Vocabulary?) {
                Log.d("HSK APP", "show: " + vocabulary.eng)
                val remoteView = RemoteViews(context.getPackageName(), R.layout.widget_layout)
                remoteView.setTextViewText(R.id.widget_eng, vocabulary.eng)
                remoteView.setTextViewText(R.id.widget_kor, vocabulary.kor)
                val componentName = ComponentName(context, VocaWidget::class.java)
                setPendingIntent(context, remoteView, widgetIds, componentName)
                var temp = ""
                for (id in widgetIds) {
                    temp += "$id "
                }
                Log.d("HSK APP", "Widget ids in showRandomVoca(): $temp")
                randomVocabulary.removeObserver(this)
            }
        })
    }

    inner class UpdateWidgetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("HSK APP", "WidgetReceiver onReceive()")
            if (intent.getAction().equals(UPDATE_WIDGET, ignoreCase = true)) {
                val widgetName = ComponentName(context.getPackageName(), VocaWidget::class.java.name)
                val widgetId = manager.getAppWidgetIds(widgetName)
                showRandomVocabulary(context, widgetId)
            }
        }
    }

    companion object {
        val UPDATE_WIDGET: String? = "action.updatewidget.update"
    }
}