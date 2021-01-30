package hsk.practice.myvoca.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.RemoteViews
import androidx.lifecycle.LiveData
import com.hsk.data.VocaRepository
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.R
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
import hsk.practice.myvoca.framework.toRoomVocabulary
import java.util.concurrent.Callable

class VocaWidget : AppWidgetProvider() {

    private var manager: AppWidgetManager? = null
    private var remoteView: RemoteViews? = null
    private val showVocaTask: AsyncTask<Void?, Void?, LiveData<RoomVocabulary?>?>? = null
    private val myTask: Callable<*>? = null
    private val receiver: UpdateWidgetReceiver? = null
    private val UPDATE_WIDGET_ID = 17

    private var vocaRepository: VocaRepository? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            Log.d("HSK APP", "VocaWidget onReceive(): ${intent.action}")
        }
        init(context)
        super.onReceive(context, intent)
        if (intent?.action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE, ignoreCase = true)) {
            val widgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: 0
            val widgetIds: IntArray?
            widgetIds = if (widgetId != -1) {
                Log.d("HSK APP", "Widget here")
                intArrayOf(widgetId)
            } else {
                Log.d("HSK APP", "Widget here 2")
                val componentName = ComponentName(context!!, VocaWidget::class.java)
                manager?.getAppWidgetIds(componentName)
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
        AppHelper.loadInstance(context!!.applicationContext)
        var temp = ""
        if (appWidgetIds != null) {
            for (id in appWidgetIds) {
                temp += "$id "
            }
        }
        Log.d("HSK APP", "Widget ids in onUpdate(): $temp")
        val componentName = ComponentName(context, VocaWidget::class.java)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
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
            remoteView = RemoteViews(context!!.packageName, R.layout.widget_layout)
        }
        if (manager == null) {
            manager = AppWidgetManager.getInstance(context)
        }
        if (vocaRepository == null) {
            vocaRepository = VocaRepository(VocaPersistenceDatabase(context!!))
        }
    }

    private fun setPendingIntent(context: Context?, remoteViews: RemoteViews?, appWidgetIds: IntArray?, componentName: ComponentName?) {
        val intent = Intent(context, VocaWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds?.get(0))
        val pendingIntent = PendingIntent.getBroadcast(context?.applicationContext, appWidgetIds?.get(0)
                ?: -1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews?.setOnClickPendingIntent(R.id.widget_voca_button, pendingIntent)
        manager?.updateAppWidget(componentName, remoteViews)
        manager?.updateAppWidget(appWidgetIds, remoteViews)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        Log.d("HSK APP", "Widget onDeleted()")
        super.onDeleted(context, appWidgetIds)
    }

    private fun showRandomVocabulary(context: Context?, widgetIds: IntArray?) {
        vocaRepository?.getRandomVocabulary()?.toRoomVocabulary()?.let {
            Log.d(AppHelper.LOG_TAG, "show: ${it.eng}")

            val remoteView = RemoteViews(context?.packageName, R.layout.widget_layout)
            remoteView.setTextViewText(R.id.widget_eng, it.eng)
            remoteView.setTextViewText(R.id.widget_kor, it.kor)
            val componentName = ComponentName(context!!, VocaWidget::class.java)
            setPendingIntent(context, remoteView, widgetIds, componentName)

            val temp = widgetIds?.joinToString() ?: ""
            Log.d("HSK APP", "Widget ids in showRandomVoca(): $temp")
        }
    }

    inner class UpdateWidgetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("HSK APP", "WidgetReceiver onReceive()")
            if (intent?.action.equals(UPDATE_WIDGET, ignoreCase = true)) {
                val widgetName = ComponentName(context!!.packageName, VocaWidget::class.java.name)
                val widgetId = manager?.getAppWidgetIds(widgetName)
                showRandomVocabulary(context, widgetId)
            }
        }
    }

    companion object {
        val UPDATE_WIDGET: String = "action.updatewidget.update"
    }
}