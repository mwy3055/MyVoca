package hsk.practice.myvoca.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.widget.RemoteViews
import androidx.lifecycle.LiveData
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.R
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
import hsk.practice.myvoca.framework.toRoomVocabulary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.Callable

@AndroidEntryPoint
class VocaWidgetProvider : AppWidgetProvider() {

    private var manager: AppWidgetManager? = null
    private var remoteView: RemoteViews? = null
    private val showVocaTask: AsyncTask<Void?, Void?, LiveData<RoomVocabulary?>?>? = null
    private val myTask: Callable<*>? = null
    private val receiver: UpdateWidgetReceiver? = null
    private val UPDATE_WIDGET_ID = 17

    private var vocaRepository: VocaRepository? = null

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        const val UPDATE_WIDGET: String = "action.updatewidget.update"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            Logger.d("VocaWidget onReceive(): ${intent.action}")
        }
        init(context)
        super.onReceive(context, intent)

        if (intent?.action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE, ignoreCase = true)) {
            val widgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: 0
            val widgetIds: IntArray? = if (widgetId != -1) {
                intArrayOf(widgetId)
            } else {
                val componentName = ComponentName(context!!, VocaWidgetProvider::class.java)
                manager?.getAppWidgetIds(componentName)
            }
            onUpdate(context, manager, widgetIds)
            showRandomVocabulary(context, widgetIds)
        }
    }

    override fun onEnabled(context: Context?) {
        Logger.d("VocaWidget onEnabled()")
        super.onEnabled(context)
        init(context)
        AppHelper.loadInstance()
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        Logger.d("VocaWidget onUpdate()")
        AppHelper.loadInstance()
        val widgetIds = appWidgetIds?.joinToString(" ") ?: ""
        Logger.d("Widget ids in onUpdate(): $widgetIds")
        val componentName = ComponentName(context!!, VocaWidgetProvider::class.java)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
        setPendingIntent(context, remoteViews, appWidgetIds, componentName)
    }

    private fun init(context: Context?) {
        if (remoteView == null) {
            remoteView = RemoteViews(context!!.packageName, R.layout.widget_layout)
        }
        if (manager == null) {
            manager = AppWidgetManager.getInstance(context)
        }
        if (vocaRepository == null) {
            vocaRepository = VocaRepository(VocaPersistenceDatabase(context?.applicationContext!!))
        }
    }

    private fun setPendingIntent(
        context: Context?,
        remoteViews: RemoteViews?,
        appWidgetIds: IntArray?,
        componentName: ComponentName?
    ) {
        val intent = Intent(context, VocaWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds?.get(0))
        val pendingIntent = PendingIntent.getBroadcast(
            context?.applicationContext, appWidgetIds?.get(0)
                ?: -1, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        remoteViews?.setOnClickPendingIntent(R.id.widget_voca_button, pendingIntent)
        manager?.updateAppWidget(componentName, remoteViews)
        manager?.updateAppWidget(appWidgetIds, remoteViews)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        Logger.d("Widget onDeleted()")
        super.onDeleted(context, appWidgetIds)
    }

    private fun showRandomVocabulary(context: Context?, widgetIds: IntArray?) =
        coroutineScope.launch {
            vocaRepository?.getRandomVocabulary()?.toRoomVocabulary()?.let {
                Logger.d("show: $it")

                val remoteView = RemoteViews(context?.packageName, R.layout.widget_layout)
                remoteView.setTextViewText(R.id.widget_eng, it.eng)
                remoteView.setTextViewText(R.id.widget_kor, it.kor)
                val componentName = ComponentName(context!!, VocaWidgetProvider::class.java)
                setPendingIntent(context, remoteView, widgetIds, componentName)
            }
        }

    inner class UpdateWidgetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Logger.d("WidgetReceiver onReceive()")
            if (intent?.action.equals(UPDATE_WIDGET, ignoreCase = true)) {
                val widgetName =
                    ComponentName(context!!.packageName, VocaWidgetProvider::class.java.name)
                val widgetId = manager?.getAppWidgetIds(widgetName)
                showRandomVocabulary(context, widgetId)
            }
        }
    }

}