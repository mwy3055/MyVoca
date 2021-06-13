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
import hsk.practice.myvoca.VocabularyImpl
import hsk.practice.myvoca.framework.RoomVocaDatabase
import hsk.practice.myvoca.framework.vocabularyImplList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VocaWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var vocaDatabase: RoomVocaDatabase

    companion object {
        private val job = Job()
        private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

        private fun getPendingIntent(context: Context, appWidgetId: Int): PendingIntent =
            Intent(
                context,
                VocaWidgetProvider::class.java
            ).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).let { intent ->
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
            vocabulary: VocabularyImpl
        ): RemoteViews = remoteViewWithPendingIntent(context, appWidgetId).apply {
            setTextViewText(R.id.widget_eng, vocabulary.eng)
            setTextViewText(R.id.widget_kor, vocabulary.kor)
        }

        fun setRemoteViewTextColor(context: Context, remoteViews: RemoteViews) =
            coroutineScope.launch {
                getTextColorFlow(context).take(1).collectLatest { color ->
                    with(remoteViews) {
                        setTextColor(R.id.widget_eng, color)
                        setTextColor(R.id.widget_kor, color)
                    }
                }
            }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Logger.d("VocaWidget onReceive(): ${intent.action}")
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                updateWidgets(context)
            }
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        job.cancel()
    }

    private fun updateWidgets(context: Context) = coroutineScope.launch {
        vocaDatabase.vocaDao()?.loadAllVocabulary()
            ?.map { it.vocabularyImplList() }
            ?.collectLatest {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(
                        context,
                        VocaWidgetProvider::class.java
                    )
                )

                val vocabulary = it.random()!!
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

    private suspend fun updateVocaWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        vocabulary: VocabularyImpl
    ) {
        Logger.d("VocaWidgetProvider.updateVocaWidget(): $vocabulary")
        val remoteViews = remoteViewWithVocabulary(context!!, widgetId, vocabulary)
        setRemoteViewTextColor(context, remoteViews).join()
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }
}