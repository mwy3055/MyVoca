package hsk.practice.myvoca.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.orhanobut.logger.Logger
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.ActivityWidgetSettingBinding
import hsk.practice.myvoca.ui.NewVocaViewModel

class WidgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWidgetSettingBinding

    private val newVocaViewModel: NewVocaViewModel by viewModels()
    private var widgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWidgetSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppHelper.loadInstance(this)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        showWidget()
    }

    private fun showWidget() {
        Logger.d("WidgetActivity showWidget()")
        newVocaViewModel.getRandomVocabulary().observeForever {
            val widgetManager = AppWidgetManager.getInstance(this@WidgetActivity)

            val remoteView = RemoteViews(packageName, R.layout.widget_layout)
            remoteView.setTextViewText(R.id.widget_eng, it?.eng)
            remoteView.setTextViewText(R.id.widget_kor, it?.kor)
            widgetManager.updateAppWidget(widgetId, remoteView)

            val intent = Intent()
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}