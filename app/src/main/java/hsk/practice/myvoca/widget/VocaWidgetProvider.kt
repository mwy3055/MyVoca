package hsk.practice.myvoca.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.R
import hsk.practice.myvoca.framework.RoomVocaDatabase
import hsk.practice.myvoca.framework.RoomVocabulary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VocaWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var vocaDatabase: RoomVocaDatabase

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        private fun getPendingIntent(context: Context, appWidgetId: Int): PendingIntent =
            Intent(context, VocaWidgetProvider::class.java).let { intent ->
                // context.applicationContext?
                PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        private fun remoteViewWithPendingIntent(context: Context, appWidgetId: Int): RemoteViews =
            RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setOnClickPendingIntent(
                    R.id.widget_voca_button,
                    getPendingIntent(context, appWidgetId)
                )
            }

        fun remoteViewWithVocabulary(
            context: Context,
            appWidgetId: Int,
            vocabulary: RoomVocabulary
        ): RemoteViews = remoteViewWithPendingIntent(context, appWidgetId).apply {
            setTextViewText(R.id.widget_eng, vocabulary.eng)
            setTextViewText(R.id.widget_kor, vocabulary.kor)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Logger.d("VocaWidget onReceive(): ${intent.action}")
        coroutineScope.launch {
            vocaDatabase.vocaDao()?.loadAllVocabulary()?.collectLatest {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(
                        context,
                        VocaWidgetProvider::class.java
                    )
                )

                val vocabulary = it.random()
                widgetIds.forEach { widgetId ->
                    updateVocaWidget(
                        context,
                        appWidgetManager,
                        widgetId,
                        vocabulary
                    )
                }
            }
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        job.cancel()
    }


    private fun updateVocaWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        vocabulary: RoomVocabulary
    ) {
        Logger.d("VocaWidgetProvider.updateVocaWidget(): $vocabulary")
        val remoteViews = remoteViewWithVocabulary(context!!, widgetId, vocabulary)
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }
}